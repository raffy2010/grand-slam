(ns electron.ffmpeg
 (:require-macros [cljs.core.async.macros :refer [go]])
 (:require [cljs.core.match :refer-macros [match]]
           [cljs.core.async :as async
                            :refer [<! >! put! chan alts! timeout]]))


(defonce child_process (js/require "child_process"))

(defonce electron       (js/require "electron"))
(defonce path           (js/require "path"))
(defonce fs             (js/require "fs"))

(def ipcMain        (.-ipcMain electron))

(def root-dir (.resolve path js/__dirname ".."))

(def ffmpeg-bin (.resolve path root-dir "bin/ffmpeg"))
(def ffprobe-bin (.resolve path root-dir "bin/ffprobe"))

(defn chanify
  "change behavior of normal node.js async apis, having
  a callback as the last argument; return a async channel"
  [origin-fn]
  (fn [& args]
    (let [out (chan)]
      (apply origin-fn
             (clj->js (conj (vec args)
                            #(put! out (js->clj [%1 %2])))))
      out)))


(def mkdir (chanify (.-mkdir fs)))

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

(defn- handle-probe
  "handle-probe"
  [event file-path]
  (go
    (let [ret (<! (spawn-process ffprobe-bin
                                 ["-loglevel" "error"
                                  "-of" "json"
                                  "-show_streams" "-show_format"
                                  file-path]))]
      (.send (.-sender event)
             "ffmpeg-probe-resp"
             (format-invoke-resp ret)))))

(defn- preview-file-dir
  "gen preview file dir"
  [file-id]
  (.resolve path
            js/__dirname
            "../"
            (str "temp/" file-id "/")))

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


(.on ipcMain "ffmpeg-probe" handle-probe)
(.on ipcMain "ffmpeg-video-preview" handle-video-preview)


