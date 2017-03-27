(ns ui.component.video-dialog
 (:require [reagent.core :as r]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [ui.state :refer [active-files]]))


(defn toggle-convert-modal
  ""
  [file]
  (let [file-id (get file "id")]
    (swap! active-files update-in [file-id :convert-mode] not)))

(defn convert-video
  ""
  [file])

(defn convert-actions
  ""
  [file]
  [:div [ui/flat-button {:label "Cancel"
                         :on-click (partial toggle-convert-modal file)}]
        [ui/flat-button {:label "Convert"
                         :primary true
                         :on-click (partial convert-video file)}]])

(defn convert-dialog
  ""
  [file]
  [ui/dialog {:title "Convert video"
              :actions (r/as-element (convert-actions file))
              :modal true
              :open (boolean (:convert-mode file))}])

