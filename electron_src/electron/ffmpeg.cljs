(ns electron.ffmpeg
 (:require-macros [cljs.core.async.macros :refer [go]])
 (:require [cljs.core.match :refer-macros [match]]
           [cljs.core.async :as async :refer [<! >! put! chan alts! timeout]]
           [electron.common :refer [ffmpeg-bin ffprobe-bin preview-dir]]
           [electron.utils :refer [mkdir]]))

(defonce child_process (js/require "child_process"))

(defonce electron       (js/require "electron"))
(defonce path           (js/require "path"))

(def ipcMain        (.-ipcMain electron))


(defn format-invoke-resp
  "format ipc invoke response"
  [ret]
  (clj->js
    (match ret
           [nil data] {:status "ok" :data data}
           [err :guard #(not (nil? %)) _] {:status "error" :msg err})))

(defn success-invoke-resp
  "format success invoke response"
  [data]
  (format-invoke-resp [nil data]))

(defn fail-invoke-resp
  "format failure invoke response"
  [err]
  (format-invoke-resp [err]))


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
         "error"
         #(swap! err-str str %))
    (.on (.-stderr newProcess)
         "end"
         #(if-not (empty? @err-str)
            (put! out
                  [err-str])))
    (.on (.-stdout newProcess)
         "data"
         #(swap! output-str str %))
    (.on (.-stdout newProcess)
         "end"
         #(put! out
                [nil @output-str]))
    out))

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
  [sender ret]
  (.send sender
        "ffmpeg-probe-resp"
        (format-invoke-resp ret)))

(defn- handle-probe
  "handle-probe"
  [event file-path]
  (go
    (let [ret (<! (probe-video file-path))]
      (respond-probe (.-sender event) ret))))

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
        (.send (.-sender event)
         "ffmpeg-video-preview-resp"
         (if (nil? mkdir-err)
           (let [[preview-err] (<! (spawn-process ffmpeg-bin
                                                  ["-i" file-name
                                                   "-r" second-rate
                                                   "-vf" "scale=-1:180"
                                                   "-vcodec" "png"
                                                   (str dirname "/preview_%d.png")]))]
              (if (nil? preview-err)
               (success-invoke-resp file-id)
               (fail-invoke-resp preview-err)))
           (fail-invoke-resp mkdir-err)))))))

(defn construct-convert-args
  [video convert-option]
  (let [file-path (aget video "format" "filename")
        base-args ["-i" file-path]]
    (match convert-option
           {:type convert-type} (conj base-args))))

(defn- handle-video-convert
  [event video convert-option]
  (go
    (let [file-id (aget video "id")
          [convert-err]
          (<! (spawn-process ffmpeg-bin
                             (construct-convert-args video
                                                     convert-option)))]
      (.send (.-sender event)
             "ffmpeg-video-convert-resp"
             (if (nil? convert-err)
               (success-invoke-resp file-id)
               (fail-invoke-resp convert-err))))))


(.on ipcMain "ffmpeg-probe" handle-probe)
(.on ipcMain "ffmpeg-video-preview" handle-video-preview)
(.on ipcMain "ffmpeg-video-convert" handle-video-convert)
