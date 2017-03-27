(ns ui.component.drag-and-drop
 (:require [cljs-react-material-ui.reagent :as ui]
           [ui.utils.common :refer [component-uid]]
           [ui.ffmpeg :refer [probe]]
           [ui.state :refer [active-files drag-files]]
           [ui.component.video-list :refer [video-list]]
           [reagent.core :refer [atom]]))

(defn get-drag-files
 "get drag files from drag event's DataTransfer"
 [event]
 (js->clj
   (.map (.from js/Array
                (-> event
                    .-dataTransfer
                    .-files))
         #(.-path %))
   :keywordize-keys true))

(defn handle-drag-enter
  [event]
  (reset! drag-files (get-drag-files event)))

(defn handle-drag-leave
  [event]
  (reset! drag-files []))

(defn handle-drag-end
  [event]
  (println "on drag end"))

(defn handle-drag-over
  [event]
  (.preventDefault event))

(defn handle-drop
  "handle file drop"
  [event]
  (do
    (.preventDefault event)
    (reset! drag-files [])
    (let [file (get-drag-files event)]
      (probe (first file)))))


(defn drag-and-drop
  "doc-string"
  []
  [:div {:class (str "drag-zoom" " "
                     (if (empty? @drag-files) "inactive" "active"))
         :on-drag-enter handle-drag-enter
         :on-drag-leave handle-drag-leave
         :on-drag-end   handle-drag-end
         :on-drag-over  handle-drag-over
         :on-drop       handle-drop}
   (if (empty? @active-files)
     [:div {:class "empty-hints"}
      [:p "drop video file here or"]
      [:p "click Menu > File > Open to select video file"]]
     [:div
      [:p {:class "video-count"}
       (str "there are "
            (count @active-files)
            " files")]
      [video-list]])])
