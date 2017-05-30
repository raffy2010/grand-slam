(ns ui.state
  (:require [reagent.core :refer [atom]]))

(defonce active-files    (atom (sorted-map)))
(defonce convert-option  (atom {}))
(defonce drag-files      (atom []))
(defonce messages        (atom (sorted-map)))
(defonce tasks           (atom (sorted-map)))
