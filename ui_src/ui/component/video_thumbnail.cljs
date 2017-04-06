(ns ui.component.video-thumbnail
 (:require [cljs.core.match :refer-macros [match]]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [ui.ffmpeg :refer [preview-src]]
           [ui.state :refer [active-files]]))

(defn- handle-video-move
  "video move handler"
  [video event]
  (let [file-id (:id video)
        img (.-target event)
        pos (.getBoundingClientRect img)
        mouse-pos (.-clientX event)
        percent (/ (- mouse-pos (.-left pos))
                   (.-width pos))
        preview-index (->> percent
                           (* 160)
                           (.round js/Math)
                           inc)]
    (preview-src file-id preview-index)))

(defn debounce
  [func span]
  (let [last-time (atom (.now js/Date))]
   (fn [& args]
     (let [now (.now js/Date)]
       (if (< span (- now @last-time))
         (apply func args)
         (reset! last-time now))))))

(defn video-thumbnail
  [video]
  (if-let [preview-src (:preview-src video)]
    [:img {:class "video-preview"
           :src preview-src
           :on-mouse-move (partial handle-video-move video)}]
    [:div {:class "video-preview"}
     [ui/refresh-indicator {:status "loading"
                            :left 60
                            :top 25}]]))

