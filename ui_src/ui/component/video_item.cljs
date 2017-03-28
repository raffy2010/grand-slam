(ns ui.component.video-item
 (:require [reagent.core :as r]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [ui.state :refer [active-files]]
           [ui.component.video-thumbnail :refer [video-thumbnail]]
           [ui.component.video-stream :refer [stream-item]]
           [ui.component.video-dialog :refer [convert-dialog]]))

(defn toggle-file-select
  "doc-string"
  [file event]
  (let [file-id (get file "id")]
    (swap! active-files update-in [file-id :selected] not)))

(defn toggle-file-detail
  "doc-string"
  [file event]
  (let [file-id (get file "id")]
    (.stopPropagation event)
    (swap! active-files update-in [file-id :detailed] not)))

(defn toggle-convert-modal
 ""
 [file event elem]
 (let [file-id (get file "id")
       mode (.-type (.-props elem))]
   (swap! active-files assoc-in [file-id :convert-mode] mode)))

(defn video-item
  "video item card"
  [file]
  [ui/card
   [:div {:class (str "video-card" (cond
                                     (:selected file) " video-selected"
                                     (:detailed file) " video-detailed"))
          :on-click (partial toggle-file-select file)}
    [video-thumbnail file]
    [:div {:class "video-info"}
     (for [field ["filename" "bit_rate" "size" "duration"]]
       ^{:key field}
       [:p (str field ": " (get-in file ["format" field]))])]
    [:div {:class "action-menu"}
     [ui/icon-button {:class (if (:detailed file) "fold" "more")
                      :on-click (partial toggle-file-detail file)}
      (if (:detailed file)
       (ic/navigation-expand-less)
       (ic/navigation-expand-more))]
     [ui/icon-menu {:use-layer-for-click-away true
                    :icon-button-element (r/as-element
                                           [ui/icon-button {:on-click #(.stopPropagation %)}
                                            (ic/action-swap-horiz)])
                    :anchor-origin {:horizontal "right" :vertical "top"}
                    :target-origin {:horizontal "right" :vertical "top"}
                    :on-item-touch-tap (partial toggle-convert-modal file)}
      [ui/menu-item {:primary-text "type"
                     :type "video-type"}]
      [ui/menu-item {:primary-text "quality"
                     :type "video-quality"}]
      [ui/menu-item {:primary-text "dimension"
                     :type "video-dimension"}]
      [ui/menu-item {:primary-text "preset"
                     :type "video-preset"}]
      [ui/menu-item {:primary-text "custom"
                     :type "advance"}]]
     [ui/icon-menu {:use-layer-for-click-away true
                    :icon-button-element (r/as-element
                                           [ui/icon-button {:on-click #(.stopPropagation %)}
                                            (ic/navigation-more-vert)])
                    :anchor-origin {:horizontal "right" :vertical "top"}
                    :target-origin {:horizontal "right" :vertical "top"}}
      [ui/menu-item {:primary-text "type"}]
      [ui/menu-item {:primary-text "quality"}]
      [ui/menu-item {:primary-text "dimension"}]
      [ui/menu-item {:primary-text "preset"}]
      [ui/menu-item {:primary-text "custom"}]]]
    [:div {:class (str "video-stream-detail"
                       (if (:more-detail file) " detail-active"))
           :on-click #(.stopPropagation %)}
     (for [stream (get file "streams")]
       ^{:key stream}
       [stream-item stream])]
    (when-not (nil? (:convert-mode file))
      [convert-dialog file])]])

