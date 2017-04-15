(ns ui.utils.common
  (:require [clojure.string :refer [join]]))

(defn common-uid
  [prefix]
  (str prefix
       (.now js/Date)
       (.round js/Math (* 10000 (.random js/Math)))))

(def component-uid (partial common-uid "component"))
(def file-uid (partial common-uid "file"))
(def msg-uid (partial common-uid "msg"))
(def task-uid (partial common-uid "task"))

(def file-size-formater [[(.pow js/Math 1000 3) "GB"]
                         [(.pow js/Math 1000 2) "MB"]
                         [1000 "Kb"]
                         [1 "B"]])

(def bitrate-formater [[1000 "kb"]
                       [1 "b"]])

(def time-formater [[3600 "h"]
                    [60 "m"]
                    [1 "s"]])

(defn format-by-level
  [end-fn formater duration]
  (loop [formater formater
         duration duration
         text-col []]
    (let [stop? (apply end-fn [duration text-col])]
      (if stop?
        (join "" text-col)
        (let [[unit text] (first formater)
              quotient (quot duration unit)
              remainder (rem duration unit)]
          (recur (rest formater)
                 remainder
                 (if (> quotient 0)
                   (conj text-col quotient text)
                   text-col)))))))

(def eager-format (partial format-by-level (fn [ret col] (= ret 0))))
(def normal-format (partial format-by-level (fn [ret col] (not (empty? col)))))


(def format-file-size (partial normal-format file-size-formater))
(def format-time (partial eager-format time-formater))
(def format-bitrate (partial normal-format bitrate-formater))


