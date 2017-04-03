(ns electron.utils
 (:require-macros [cljs.core.async.macros :refer [go]])
 (:require [cljs.core.async :as async
            :refer [<! >! put! chan alts! timeout]]))

(defonce electron       (js/require "electron"))

(defonce dialog       (.-dialog electron))

(defonce fs (js/require "fs"))

(defn chanify
  "change behavior of normal node.js async apis, having
  a callback as the last argument; return a async channel"
  [origin-fn]
  (fn [& args]
    (let [out (chan)]
      (apply origin-fn
             (clj->js (conj (vec args)
                            (fn [& callback-args]
                              (put! out (js->clj callback-args))))))
      out)))

(def mkdir (chanify (.-mkdir fs)))
(def fs-access (chanify (.-access fs)))
(def fs-unlink (chanify (.-unlink fs)))

(def show-save-dialog (chanify (.-showSaveDialog dialog)))
