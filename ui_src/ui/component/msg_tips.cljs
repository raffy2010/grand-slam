(ns ui.component.msg-tips
 (:require [cljs-react-material-ui.reagent :as ui]
           [ui.utils.common :refer [component-uid]]
           [ui.state :refer [err-msgs]]))

(defn remove-err-msg
 [msg-id]
 (reset! err-msgs
         (filter #(not= (:msg-id %) msg-id)
                 @err-msgs)))

(defn msg-tips
  []
  [:div
   (for [msg @err-msgs]
    ^{:key (component-uid)}
    [ui/snackbar {:open true
                  :message (:text msg)
                  :auto-hide-duration 4000
                  :on-request-close (partial remove-err-msg (:msg-id msg))}])])

