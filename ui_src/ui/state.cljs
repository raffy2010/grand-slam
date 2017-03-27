(ns ui.state
  (:require [reagent.core :refer [atom]]))

(defonce active-files    (atom (sorted-map)))
(defonce drag-files      (atom []))
(defonce err-msgs        (atom []))
