(ns ui.state
  (:require [reagent.core :refer [atom]]))

(defonce active-files    (atom (sorted-map)))
(defonce convert-option  (atom {:type ""}))
(defonce drag-files      (atom []))
(defonce err-msgs        (atom []))
