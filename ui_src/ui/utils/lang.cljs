(ns ui.utils.lang)

(defn js->clj-kw
  [data]
  (js->clj data :keywordize-keys true))
