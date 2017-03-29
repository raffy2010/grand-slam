(ns ui.ffmpeg
  (:require [cljs.core.match :refer-macros [match]]
   [ui.state :refer [active-files err-msgs convert-option]]
   [ui.utils.common :refer [file-uid msg-uid]]))

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
  [file]
  (.send ipcRenderer
         "ffmpeg-video-convert"
         (clj->js file)
         (clj->js @convert-option)))


(defn preview
 [video]
 (.send ipcRenderer "ffmpeg-video-preview" (clj->js video)))

(defn- handle-probe-result
  [event ret]
  (match (parse-invoke-resp ret)
         [err] (swap! err-msgs conj {:msg-id (msg-uid)
                                     :text err})
         [nil data] (let [file-obj (.parse js/JSON data)
                          file-id (file-uid)]
                      (set! (.-id file-obj)
                            file-id)
                      (swap! active-files
                             assoc file-id (js->clj file-obj))
                      (preview file-obj)))) ;setup preview file

(.on ipcRenderer "ffmpeg-probe-resp" handle-probe-result)

(defn- handle-preview-result
  [event ret]
  (match (parse-invoke-resp ret)
         [err] (swap! err-msgs conj {:msg-id (msg-uid)
                                     :text err})
         [nil file-id] (preview-src file-id 1)))

(.on ipcRenderer "ffmpeg-video-preview-resp" handle-preview-result)
