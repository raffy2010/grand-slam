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
                        :ogg  {:video ["theora" "mpeg1" "mpeg2" "wmv"]
                               :audio ["vorbis" "opus" "mp3" "flac"]}
                        :webm {:video ["vp8" "vp9"]
                               :audio ["vorbis" "opus"]}
                        :flv  {:video ["h264"]
                               :audio ["aac" "mp3"]}})

;;; Based on quality produced from high to low
;;; libopus > libvorbis >= libfdk_aac > aac > libmp3lame >= eac3/ac3 > libtwolame > vorbis > mp2 > wmav2/wmav1

(def codec-map {:mpeg1 {:encoder "mpeg1video"
                        :param ["-q:v" "3"]}
                :mpeg2 {:encoder "mpeg2video"
                        :param ["-q:v" "3"]}
                :mpeg4 {:encoder "mpeg4"
                        :param ["-q:v" "3"]}
                :h264  {:encoder "libx264"
                        :param ["-preset" "medium" "-crf" "23"]}
                :h265  {:encoder "libx265"
                        :param ["-preset" "medium" "-crf" "28"]}
                :wmv  {:encoder "wmv2"}
                :theora  {:encoder "libtheora"
                          :param ["-q:v" "7"]}
                :msmpeg4v2  {:encoder "msmpeg4v2"}
                :vp8  {:encoder "libvpx"
                       :param ["-crf" "10" "-b:v" "2M"]}
                :vp9  {:encoder "libvpx-vp9"
                       :param ["-crf" "10" "-b:v" "2M"]}
                :mp3  {:encoder "libmp3lame"
                       :param ["-q:a" "3"]}
                :wma  {:encoder "wmav2"}
                :vorbis  {:encoder "libvorbis"
                          :param ["-q:a" "5"]}
                :opus  {:encoder "libopus"}
                :aac  {:encoder "libfdk_aac"
                       :param ["-vbr" "3"]}
                :ac-3  {:encoder "ac3"}
                :dts  {:encoder "dca"}
                :flac  {:encoder "flac"}
                :mlp  {:encoder "mlp"}
                :alac  {:encoder "alac"}})

(defn col-contains?
 [x col]
 (some #(= x %) col))

(defn codec-flag
  [codec-type]
  (match codec-type
         :video "-c:v"
         :audio "-c:a"
         :subtitle "-c:s"))

(defn get-encoder-info
  [field codec]
  (get-in codec-map [(keyword codec) field]))

(def get-encoder-name (partial get-encoder-info :encoder))
(def get-encoder-param (partial get-encoder-info :param))

(defn gen-target-codec
  [codec-type origin-codec target]
  (let [flag (codec-flag codec-type)
        target-codecs-list (get-in container-support [(keyword target) codec-type] [])
        target-codec (if (col-contains? origin-codec
                                        target-codecs-list)
                       "copy"
                       (first target-codecs-list))]
    (match target-codec
           "copy" [flag "copy"]
           :else
           (into [] (concat [flag (get-encoder-name target-codec)]
                            (get-encoder-param target-codec))))))

(def gen-target-video-codec (partial gen-target-codec :video))
(def gen-target-audio-codec (partial gen-target-codec :audio))

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
                 target-video-codec (-> video
                                        :streams
                                        get-video-codec
                                        (gen-target-video-codec convert-type))
                 target-audio-codec (-> video
                                        :streams
                                        get-audio-codec
                                        (gen-target-audio-codec convert-type))]
             (into [] (concat base-args
                              target-video-codec
                              target-audio-codec
                              [target-path])))

           [{:quality height}]
           (into [] (concat base-args
                            ["-vf" (str "scale=-1:" height)]
                            [(:target convert-option)])))))


