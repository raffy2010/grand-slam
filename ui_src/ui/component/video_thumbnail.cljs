(ns ui.component.video-thumbnail
 (:require [cljs.core.match :refer-macros [match]]
           [ui.ffmpeg :refer [preview-src]]
           [ui.state :refer [active-files]]))

(defn- handle-video-move
  "video move handler"
  [video event]
  (let [file-id (get video "id")
        img (.-target event)
        pos (.getBoundingClientRect img)
        mouse-pos (.-clientX event)
        percent (/ (- mouse-pos (.-left pos))
                   (.-width pos))
        preview-index (.round js/Math (* 160 percent))]
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
  [:img {:class "video-preview"
         :src (get video :preview-src)
         :on-mouse-move (partial handle-video-move
                                 video)}])

