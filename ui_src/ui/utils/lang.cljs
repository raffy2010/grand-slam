(ns ui.utils.lang)

(defn js->clj-kw
  [data]
  (js->clj data :keywordize-keys true))

(defn js-vals
  "return js object values as a vector"
  [obj & obj-keys]
  (let [values (.map (js-keys obj) #(aget obj %))]
    (do
      (.log js/console values))))
      ;(println values))))
