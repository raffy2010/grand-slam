(ns ui.utils.lang)

;(defn js->clj-full
  ;"transform js data to clj data, full support to js"
  ;[js-data]
  ;(cond
    ;(array? js-data)
    ;(js->clj js-data)
    ;(js-obj? js-data)
    ;(js-keys js-data)))

;(defn js-obj?
  ;"check js object"
  ;[data]
  ;(= (typeof data) "object"))


(defn js-vals
  "return js object values as a vector"
  [obj & obj-keys]
  (let [values (.map (js-keys obj) #(aget obj %))]
    (do
      (.log js/console values))))
      ;(println values))))
