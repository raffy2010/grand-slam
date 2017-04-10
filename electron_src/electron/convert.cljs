(ns electron.convert
  (:require [cljs.core.match :refer-macros [match]]))

(def container-support {:avi  {:video ["h264" "mpeg1" "mpeg2" "mpeg4" "wmv" "theora" "msmpeg4v2" "vp8"]
                               :audio ["aac" "mp3" "ac-3" "wma" "dts"]}
                        :mp4  {:video ["h264" "mpeg1" "mpeg2" "mpeg4" "h265" "wmv" "theora" "msmpeg4v2" "vp8" "vp9"]
                               :audio ["opus" "aac" "mp3" "ac-3" "wma" "dts" "alac" "dts-hd"]}
                        :3gp  {:video ["h264" "mpeg4"]
                               :audio ["aac"]}
                        :mpg  {:video ["mpeg1" "mpeg2"]
                               :audio ["mp3"]}
                        :ts   {:video ["mpeg1" "mpeg2"]
                               :audio ["aac" "mp3" "ac3"]}
                        :mkv  {:video ["h264" "mpeg1" "mpeg2" "mpeg4" "h265" "wmv" "theora" "msmpeg4v2" "vp8" "vp9"]
                               :audio ["opus" "vorbis" "mp3" "aac" "ac-3" "wma" "dts" "flac" "mlp" "alac" "dts-hd"]}
                        :ogg  {:video ["h264" "mpeg1" "mpeg2" "mpeg4" "wmv" "theora"]
                               :audio ["opus" "vorbis" "mp3" "flac"]}
                        :webm {:video ["vp8" "vp9"]
                               :audio ["opus" "vorbis"]}
                        :flv  {:video ["h264"]
                               :audio ["aac" "mp3"]}})

;;; Based on quality produced from high to low
;;; libopus > libvorbis >= libfdk_aac > aac > libmp3lame >= eac3/ac3 > libtwolame > vorbis > mp2 > wmav2/wmav1

(def codec-map {:mpeg1 "mpeg1video"
                :mpeg2 "mpeg2video"
                :mpeg4 "mpeg4"
                :h264  "libx264"
                :h265  "libx265"
                :wmv  "wmv2"
                :theora  "libtheora"
                :msmpeg4v2  "msmpeg4v2"
                :vp8  "libvpx"
                :vp9  "libvpx-vp9"
                :mp3  "libmp3lame"
                :wma  "wmav2"
                :vorbis  "libvorbis"
                :opus  "libopus"
                :aac  "libfdk_aac"
                :ac-3  "ac3"
                :dts  "dca"
                :flac  "flac"
                :mlp  "mlp"
                :alac  "alac"})

(defn col-contains?
 [x col]
 (some #(= x %) col))

(defn gen-video-audio-codec
  [file target]
  (let [{:keys [video audio]} file
        target-video-list (get-in container-support [(keyword target) :video])
        target-audio-list (get-in container-support [(keyword target) :audio])
        target-video (if (col-contains? video target-video-list)
                       "copy"
                       ((keyword (first target-video-list)) codec-map))
        target-audio (if (col-contains? audio target-audio-list)
                       "copy"
                       ((keyword (first target-audio-list)) codec-map))]
    ["-c:v" target-video "-c:a" target-audio]))

(defn get-codec
  "get codec from stream"
  [stream-type streams]
  (->> streams
       (filter #(= (:codec_type %) stream-type))
       first
       :codec_name))

(def get-video-codec (partial get-codec "video"))
(def get-audio-codec (partial get-codec "audio"))
(def get-subtitle-codec (partial get-codec "subtitle"))


(defn construct-convert-args
  [video convert-option]
  (let [file-path (get-in video [:format :filename])
        base-args ["-y" "-stats"
                   "-loglevel" "error"
                   "-i" file-path]]
   (match [convert-option]
          [{:type convert-type}]
          (let [target-path (str (:target convert-option) "." convert-type)
                video-codec (get-video-codec (:streams video))
                audio-codec (get-audio-codec (:streams video))
                codecs (gen-video-audio-codec
                         {:video video-codec
                          :audio audio-codec}
                         convert-type)]
            (into [] (concat base-args codecs [target-path]))))))


