(ns electron.ffmpeg
 (:require-macros [cljs.core.async.macros :refer [go go-loop]])
 (:require [cljs.core.match :refer-macros [match]]
           [cljs.core.async :as async :refer [<! >! put! chan alts! timeout]]
           [electron.common :refer [ffmpeg-bin ffprobe-bin preview-dir]]
           [electron.utils :refer [mkdir fs-unlink show-save-dialog]]
           [electron.convert :refer [construct-convert-args]]))

(defonce child_process (js/require "child_process"))

(defonce electron       (js/require "electron"))
(defonce path           (js/require "path"))

(defonce dialog       (.-dialog electron))

(def ipcMain        (.-ipcMain electron))

(defn format-invoke-resp
  "format ipc invoke response"
  [ret]
  (clj->js
    (match ret
           [nil data] {:status "ok" :data data}
           [err :guard #(not (nil? %))] {:status "error" :msg err})))

(defn success-invoke-resp
  "format success invoke response"
  [data]
  (format-invoke-resp [nil data]))

(defn fail-invoke-resp
  "format failure invoke response"
  [err]
  (format-invoke-resp [err nil]))


(defn spawn-process
  "spawn new process helper"
  [cmd args]
  (let [out (chan)
        output-str (atom "")
        err-str    (atom "")
        newProcess (.spawn child_process
                           (clj->js cmd)
                           (clj->js args))]
    (.on (.-stderr newProcess)
         "data"
         #(swap! err-str str %))
    (.on (.-stdout newProcess)
         "data"
         #(swap! output-str str %))
    (.on newProcess
         "exit"
         (fn [code]
           (put! out (match code
                            0 [nil @output-str]
                            1 [@err-str]))))
    out))

(defn progress-process
  [cmd args]
  (let [out (chan)
        err-str (atom "")
        output-str (atom "")
        newProcess (.spawn child_process
                           (clj->js cmd)
                           (clj->js args))]
    (.on (.-stderr newProcess)
         "data"
         (fn [err-data]
           (do
             (swap! err-str str err-data)
             (put! out {:progress (str err-data)}))))
    (.on (.-stdout newProcess)
         "data"
         #(swap! output-str str %))
    (.on newProcess
         "exit"
         (fn [code]
           (put! out {:exit (match code
                                     0 [nil @output-str]
                                     1 [@err-str]
                                     255 ["cancel"])})))
    [newProcess out]))

(defn probe-video
  "probe video"
  [file-path]
  (spawn-process ffprobe-bin
                 ["-loglevel" "error"
                  "-of" "json"
                  "-show_streams" "-show_format"
                  file-path]))

(defn respond-probe
  "respond to probe request"
  [ret sender]
  (->> ret
       format-invoke-resp
       (.send sender "ffmpeg-probe-resp")))

(defn- handle-probe
  "handle-probe"
  [event file-path]
  (go
    (-> file-path
        probe-video
        <!
        (respond-probe (.-sender event)))))

(defn- preview-file-dir
  "gen preview file dir"
  [file-id]
  (.resolve path
            preview-dir
            file-id))

(defn- preview-file-name
  "gen preview file name"
  [time file-id]
  (.resolve path
           (preview-file-dir file-id)
           (str "preview_" time ".png")))

(defn- handle-video-preview
  "handle video preview"
  [event video]
  (let [file-id (aget video "id")
        file-name (aget video "format" "filename")
        dirname (preview-file-dir file-id)
        second-rate (/ 160 (aget video "format" "duration"))]
    (go
      (let [[mkdir-err] (<! (mkdir dirname))]
        (-> event
            .-sender
            (.send
              "ffmpeg-video-preview-resp"
              (if-not (nil? mkdir-err)
               (fail-invoke-resp mkdir-err)
               (let [[preview-err] (<! (->> ["-loglevel" "error"
                                              "-i" file-name
                                              "-r" second-rate
                                              "-vf" "scale=-1:180"
                                              "-vcodec" "png"
                                              (str dirname "/preview_%d.png")]
                                            (spawn-process ffmpeg-bin)))]
                 (if (nil? preview-err)
                   (success-invoke-resp file-id)
                   (fail-invoke-resp preview-err))))))))))

(defn send-channel-back
  "send channel back with response dadta"
  [event channel data]
  (.send (.-sender event)
         channel
         data))


(defn convert-progress-data
  "generate convert task progress data"
  [video progress-line]
  (let [[_ hours minutes seconds mili-seconds] (re-find #".*time=(\d{2}):(\d{2}):(\d{2}).(\d{2}).*" progress-line)
        duration (-> video
                     (get-in [:format :duration])
                     js/parseFloat
                     (* 1000)
                     (js/parseInt 10))
        current (reduce (fn [ret [v factor]]
                          (+ ret (* factor (js/parseInt v 10))))
                        0
                        (partition 2 [hours 3600000
                                      minutes 60000
                                      seconds 1000
                                      mili-seconds 1]))]
    (->> (/ current duration)
         (* 100)
         (.floor js/Math))))


(defn- handle-video-convert
  [event task-id video convert-option]
  (let [js-video (js->clj video :keywordize-keys true)
        js-convert-option (js->clj convert-option :keywordize-keys true)
        file-id (:id js-video)
        progress-notify (partial send-channel-back
                                 event
                                 "ffmpeg-video-convert-progress-resp")
        finish-notify (partial send-channel-back
                               event
                               "ffmpeg-video-convert-finish-resp")
        calc-progress (partial convert-progress-data js-video)
        convert-args (construct-convert-args js-video
                                             js-convert-option)
        [process out] (progress-process ffmpeg-bin convert-args)]
    (->> {:file-id file-id
          :task-id task-id
          :process-data {:pid (.-pid process)
                         :progress 0}}
         success-invoke-resp
         (send-channel-back event "ffmpeg-video-convert-begin-resp"))
    (go-loop []
      (let [ret (<! out)]
        (match [ret]
               [{:progress progress-data}]
               (do
                 (->> progress-data
                      calc-progress
                      (assoc {:task-id task-id} :progress)
                      success-invoke-resp
                      progress-notify)
                 (recur))

               [{:exit exit-data}]
               (match exit-data
                      ["cancel"] (fs-unlink (str (:target js-convert-option) "." (:type js-convert-option)))
                      [convert-err] (->> convert-err
                                         fail-invoke-resp
                                         finish-notify)
                      [nil _] (->> {:file-id file-id
                                    :task-id task-id}
                                   success-invoke-resp
                                   finish-notify)))))))

(defn handle-video-export
  [event file convert-option]
  (go
    (let [[ret] (<! (show-save-dialog {}))]
      (when-not (nil? ret)
        (aset convert-option "target" ret)
        (->> {:file file
              :convert-option convert-option}
             success-invoke-resp
             (.send (.-sender event) "ffmpeg-video-export-resp"))))))

(defn handle-video-cancel-convert
  [event pid]
  (.kill js/process pid))

(.on ipcMain "ffmpeg-probe" handle-probe)
(.on ipcMain "ffmpeg-video-preview" handle-video-preview)
(.on ipcMain "ffmpeg-video-convert" handle-video-convert)
(.on ipcMain "ffmpeg-video-export" handle-video-export)
(.on ipcMain "ffmpeg-video-cancel-convert" handle-video-cancel-convert)

