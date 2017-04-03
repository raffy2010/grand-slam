(ns ui.component.msg-tips
 (:require [cljs-react-material-ui.reagent :as ui]
           [ui.state :refer [messages]]))

(defn remove-msg
 [msg-id]
 (swap! messages dissoc msg-id))

(defn msg-tips
  []
  [:div
   (for [msg (vals @messages)]
    ^{:key (:msg-id msg)}
    [ui/snackbar {:open true
                  :message (str (:type msg) " " (:text msg))
                  :auto-hide-duration 4000
                  :on-request-close (partial remove-msg (:msg-id msg))}])])

