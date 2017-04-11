(ns ui.component.video-dialog
 (:require [reagent.core :as r]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [cljs.core.match :refer-macros [match]]
           [ui.state :refer [active-files convert-option]]
           [ui.ffmpeg :refer [export-video]]))


(def video-types [{:name "mp4"
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

(defn update-convert-type
  "update convert video type"
  [event, index, value]
  (swap! convert-option assoc-in [:type] value))

(defn convert-type
  [file]
  [:div
    [:p "Please select target type: "]
    [ui/drop-down-menu {:max-height 300
                        :value (:type @convert-option)
                        :on-change update-convert-type}
      (for [video-type video-types]
        ^{:key video-type}
        [ui/menu-item {:value (:value video-type)
                       :label (:name video-type)
                       :primary-text (:name video-type)}])]])

(defn convert-dialog
  [file]
  [ui/dialog {:title "Convert video"
              :actions (r/as-element (convert-actions file))
              :modal true
              :open (boolean (:convert-mode file))}
   (match (:convert-mode file)
    "video-type" (convert-type file)
    :else nil)])

