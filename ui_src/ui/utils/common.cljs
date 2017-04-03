(ns ui.utils.common)

(defn common-uid
  [prefix]
  (str prefix
       (.now js/Date)
       (.round js/Math (* 10000 (.random js/Math)))))

(def component-uid (partial common-uid "component"))
(def file-uid (partial common-uid "file"))
(def msg-uid (partial common-uid "msg"))
(def task-uid (partial common-uid "task"))

