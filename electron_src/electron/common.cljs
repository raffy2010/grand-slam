(ns electron.common)

(defonce path           (js/require "path"))

(def electron       (js/require "electron"))
(def app            (.-app electron))

(def root-dir (.resolve path js/__dirname ".."))

(def app-data-dir (.getPath app "userData"))

(def ffmpeg-dir (.resolve path root-dir "bin"))
(def ffmpeg-bin (.resolve path ffmpeg-dir "ffmpeg"))
(def ffprobe-bin (.resolve path ffmpeg-dir "ffprobe"))

(def preview-dir (.resolve path app-data-dir "preview"))
