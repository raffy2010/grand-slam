(ns electron.utils
 (:require-macros [cljs.core.async.macros :refer [go]])
 (:require [cljs.core.async :as async
            :refer [<! >! put! chan alts! timeout]]))


(defonce fs (js/require "fs"))

(defn chanify
  "change behavior of normal node.js async apis, having
  a callback as the last argument; return a async channel"
  [origin-fn]
  (fn [& args]
    (let [out (chan)]
      (apply origin-fn
             (clj->js (conj (vec args)
                            #(put! out (js->clj [%1 %2])))))
      out)))

(def mkdir (chanify (.-mkdir fs)))
(def fs-access (chanify (.-access fs)))

