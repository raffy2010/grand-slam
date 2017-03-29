(ns electron.init
 (:require-macros [cljs.core.async.macros :refer [go]])
 (:require [cljs.core.async :as async :refer [<! >! put! chan alts! timeout]]
           [electron.common :refer [preview-dir]]
           [electron.utils :refer [mkdir]]))

(defn prepare-preview-dir
  []
  (mkdir preview-dir))
