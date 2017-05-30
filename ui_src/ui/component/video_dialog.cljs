(ns ui.component.video-dialog
 (:require [reagent.core :as r]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [cljs.core.match :refer-macros [match]]
           [ui.state :refer [active-files convert-option]]
           [ui.ffmpeg :refer [export-video extract-video-stream]]))


(def video-container-list [{:name "mp4"
                            :value "mp4"}
                           {:name "mkv"
                            :value "mkv"}
                           {:name "avi"
                            :value "avi"}
                           {:name "mpg"
                            :value "mpg"}
                           {:name "ts"
                            :value "ts"}
                           {:name "webm"
                            :value "webm"}
                           {:name "ogg"
                            :value "ogg"}
                           {:name "flv"
                            :value "flv"}
                           {:name "3gp"
                            :value "3gp"}])

(def video-quality-list [{:name "2160p 4k"
                          :value "2160"}
                         {:name "1440p 2k"
                          :value "1440"}
                         {:name "1080p"
                          :value "1080"}
                         {:name "720p"
                          :value "720"}
                         {:name "480p"
                          :value "480"}
                         {:name "360p"
                          :value "360"}
                         {:name "240p"
                          :value "240"}
                         {:name "144p"
                          :value "144"}])

(defn format-quality
  [file]
  (let [video (extract-video-stream file)
        width (:width video)
        height (:height video)]
    (str width "x" height)))

(defn toggle-convert-modal
  [file]
  (let [file-id (:id file)]
    (reset! convert-option {})
    (swap! active-files assoc-in [file-id :convert-mode] nil)))

(defn select-output-target
  "convert video file"
  [file]
  (export-video file))

(defn convert-actions
  [file]
  [:div [ui/flat-button {:label "Cancel"
                         :on-click (partial toggle-convert-modal file)}]
        [ui/flat-button {:label "Convert"
                         :primary true
                         :on-click (partial select-output-target file)}]])

(defn update-convert-container
  "update convert video type"
  [event, index, value]
  (swap! convert-option assoc-in [:type] value))

(defn update-convert-quality
  "update convert video type"
  [event, index, value]
  (swap! convert-option assoc-in [:quality] value))

(defn convert-container
  [file]
  [:div
    [:p "Please select target type: "]
    [ui/drop-down-menu {:max-height 300
                        :value (:type @convert-option)
                        :on-change update-convert-container}
      (for [video-container video-container-list]
        ^{:key video-container}
        [ui/menu-item {:value (:value video-container)
                       :label (:name video-container)
                       :primary-text (:name video-container)}])]])

(defn convert-quality
  [file]
  [:div
    [:p (str "File quality: " (format-quality file))]
    [:p "Please select target quality:"]
    [ui/drop-down-menu {:max-height 300
                        :value (:quality @convert-option)
                        :on-change update-convert-quality}
      (for [video-quality video-quality-list]
       ^{:key video-quality}
       [ui/menu-item {:value (:value video-quality)
                      :label (:name video-quality)
                      :primary-text (:name video-quality)}])]])

(defn convert-dialog
  [file]
  [ui/dialog {:title "Convert video"
              :actions (r/as-element (convert-actions file))
              :modal true
              :open (boolean (:convert-mode file))}
   (match (:convert-mode file)
          "video-type" (convert-container file)
          "video-quality" (convert-quality file)
          :else nil)])

