(ns ui.component.video-list
  (:require [ui.state :refer [active-files]]
            [ui.utils.common :refer [component-uid]]
            [ui.component.video-item :refer [video-item]]))

(defn video-list
  []
  [:div
   (for [file (vals @active-files)]
     ^{:key (get file "id")}
     [video-item file])])
