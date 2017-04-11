(ns ui.ffmpeg
  (:require [cljs.core.match :refer-macros [match]]
            [ui.state :refer [active-files
                              messages
                              convert-option
                              tasks]]
            [ui.utils.common :refer [file-uid
                                     msg-uid
                                     task-uid]]
            [ui.utils.lang :refer [js->clj-kw]]))

(defonce path (js/require "path"))

(defonce electron (js/require "electron"))
(defonce app      (.-app (.-remote electron)))

(def preview-dir (.resolve path
                      (.getPath app "userData")
                      "preview"))

(defonce ipcRenderer (.-ipcRenderer electron))

(defn probe
  [file]
  (.send ipcRenderer "ffmpeg-probe" file))

(defn parse-invoke-resp
  [resp]
  (.log js/console resp)
  (match (js->clj resp :keywordize-keys true)
         {:status "ok" :data data} [nil data]
         {:status "error" :msg err} [err]))

(defn clean-file
  "doc-string"
  [file-id]
  (swap! active-files dissoc file-id)
  (.send ipcRenderer "ffmpeg-clean-preview" file-id))


(defn preview-src
  "gen preview image src with preview time index"
  [file-id index]
  (swap! active-files
         assoc-in
         [file-id :preview-src]
         (str preview-dir
              "/"
              file-id
              "/preview_" index ".png")))

(defn convert-video
  [file convert-option]
  (let [task-id (task-uid)]
    (swap! tasks assoc task-id {:id task-id
                                :file-id (:id file)})
    (.send ipcRenderer
           "ffmpeg-video-convert"
           task-id
           (clj->js file)
           (clj->js convert-option))))

(defn export-video
  [file]
  (.send ipcRenderer
         "ffmpeg-video-export"
         (clj->js file)
         (clj->js @convert-option)))

(defn cancel-convert
  [pid]
  (.send ipcRenderer
         "ffmpeg-video-cancel-convert"
         (clj->js pid)))


(defn preview
 [video]
 (.send ipcRenderer "ffmpeg-video-preview" (clj->js video)))

(defn add-msg
  [msg-type text]
  (let [new-msg-id (msg-uid)]
    (swap! messages
           assoc
           new-msg-id
           {:msg-id new-msg-id
            :type msg-type
            :text text})))

(def add-success-msg (partial add-msg :success))
(def add-error-msg (partial add-msg :error))
(def add-info-msg (partial add-msg :info))

(defn check-stream-type
  [stream-type file]
  (->> file
       :streams
       (filter #(= stream-type
                   (:codec_type %)))
       empty?
       not))


(def check-video-stream (partial check-stream-type "video"))
(def check-audio-stream (partial check-stream-type "audio"))
(def check-subtitle-stream (partial check-stream-type "subtitle"))

(defn- handle-probe-result
  [event ret]
  (match (parse-invoke-resp ret)
         [err] (add-error-msg err)
         [nil data] (let [file-obj (->> data
                                        (.parse js/JSON)
                                        js->clj-kw)]
                      (if-not (check-video-stream file-obj)
                        (add-error-msg "current version only support file with video stream")
                        (let [file-id (file-uid)
                              file-with-id (assoc file-obj :id file-id)]
                          (swap! active-files
                                 assoc file-id file-with-id)
                          (preview file-with-id)))))) ;setup preview file

(.on ipcRenderer "ffmpeg-probe-resp" handle-probe-result)

(defn- handle-preview-result
  [event ret]
  (match (parse-invoke-resp ret)
         [err] (add-error-msg err)
         [nil file-id] (preview-src file-id 1)))

(.on ipcRenderer "ffmpeg-video-preview-resp" handle-preview-result)

(defn- handle-export-result
  [event ret]
  (match (parse-invoke-resp ret)
         [nil {:file file
               :convert-option convert-option}]
         (convert-video file
                        convert-option)))

(.on ipcRenderer "ffmpeg-video-export-resp" handle-export-result)

(defn handle-convert-begin
  [event ret]
  (match (parse-invoke-resp ret)
         [err] (add-error-msg err)
         [nil {:file-id file-id
               :task-id task-id
               :process-data process-data}]
         (do
           (swap! tasks assoc-in [task-id :process] process-data)
           (swap! active-files
                  assoc-in
                  [file-id :convert-mode] nil))))

(defn handle-convert-progress
  [event ret]
  (match (parse-invoke-resp ret)
         [err] (add-error-msg err)
         [nil {:task-id task-id
               :progress progress}]
         (swap! tasks assoc-in [task-id :process :progress] progress)))

(defn- handle-convert-result
 [event ret]
 (match (parse-invoke-resp ret)
        [err] (add-error-msg err)
        [nil {:file-id file-id
              :task-id task-id}]
        (do
          (swap! tasks dissoc task-id)
          (add-success-msg "convert complete"))))

(.on ipcRenderer "ffmpeg-video-convert-begin-resp" handle-convert-begin)
(.on ipcRenderer "ffmpeg-video-convert-progress-resp" handle-convert-progress)
(.on ipcRenderer "ffmpeg-video-convert-finish-resp" handle-convert-result)
