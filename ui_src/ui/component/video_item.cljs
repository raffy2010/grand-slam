(ns ui.component.video-item
 (:require [cljs.core.match :refer-macros [match]]
           [reagent.core :as r]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [reanimated.core :as anim]
           [ui.state :refer [active-files tasks]]
           [ui.ffmpeg :refer [cancel-convert clean-file]]
           [ui.component.video-thumbnail :refer [video-thumbnail]]
           [ui.component.video-stream :refer [stream-item]]
           [ui.component.video-dialog :refer [convert-dialog]]))

(defn toggle-file-select
  "doc-string"
  [file event]
  (let [file-id (:id file)]
    (swap! active-files update-in [file-id :selected] not)))

(defn toggle-file-detail
  "doc-string"
  [file event]
  (let [file-id (:id file)]
    (.stopPropagation event)
    (swap! active-files update-in [file-id :detailed] not)))

(defn toggle-convert-modal
 "open video conversion modal"
 [file event elem]
 (let [file-id (:id file)
       mode (.-type (.-props elem))]
   (swap! active-files assoc-in [file-id :convert-mode] mode)))

(defn handle-more
  "handle more action with file"
  [file event elem]
  (let [file-id (:id file)
        action (-> elem
                   .-props
                   .-action)]
    (match action
           "remove"
           (clean-file file-id))))

(defn handle-cancel
  [task event]
  (do
    (.stopPropagation event)
    (swap! tasks dissoc (:id task))
    (cancel-convert (get-in task [:process :pid]))))

(defn find-task
  [file]
  (let [file-id (:id file)]
    (->> @tasks
         vals
         (filter #(= file-id
                     (:file-id %)))
         first)))

(defn cancel-btn
  [task]
  [:div {:style {:top 0
                 :right 0}}
   [ui/icon-button
    {:on-click (partial handle-cancel task)}
    (ic/content-clear)]])

(defn action-btns
  [file]
  [:div {:style {:top 0
                 :right 0
                 :width 144}}
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
                  :target-origin {:horizontal "right" :vertical "top"}
                  :on-item-touch-tap (partial handle-more file)}
    [ui/menu-item {:primary-text "remove"
                   :action "remove"}]]])

(defn video-item
  "video item card"
  [file]
  (let [task (find-task file)]
    [ui/card
     [:div {:class (str "video-card" (cond
                                       (:selected file) " video-selected"
                                       (:detailed file) " video-detailed"))
            :on-click (partial toggle-file-select file)}
      [video-thumbnail file]
      [:div {:class "video-info"}
       (for [field [:filename :bit_rate :size :duration]]
         ^{:key field}
         [:p (str field ": " (get-in file [:format field]))])]
      (if-not (nil? task) [ui/linear-progress {:class "convert-progress-bar"
                                               :mode "determinate"
                                               :value (get-in task [:process :progress])}])
      [:div {:class "action-menu"}
         [anim/css-transition-group
          {:transition-name "cancel-btn"
           :transition-enter-timeout 500
           :transition-leave-timeout 500}
          (if-not (nil? task)
            (cancel-btn task))]
         [anim/css-transition-group
          {:transition-name "action-btn"
           :transition-enter-timeout 500
           :transition-leave-timeout 500}
          (if (nil? task)
            (action-btns file))]]
      [:div {:class (str "video-stream-detail"
                         (if (:more-detail file) " detail-active"))
             :on-click #(.stopPropagation %)}
       (for [stream (:streams file)]
         ^{:key stream}
         [stream-item stream])]
      (when-not (nil? (:convert-mode file))
        [convert-dialog file])]]))

