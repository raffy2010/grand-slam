(ns ui.utils.fs)

(defonce fs (js/require "fs"))

(defn stat
  "get fs stat"
  ^{:pre [(string? path)]}
  [path cb]
  (.stat fs
         path
         cb))
         ;#(apply cb
                 ;(js->clj %1)
                 ;(js->clj %2 :keywordize-keys true))))

