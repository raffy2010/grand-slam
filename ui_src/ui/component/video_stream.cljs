(ns ui.component.video-stream)

(defn stream-item
  "doc-string"
  [stream]
  [:div {:class "video-stream"}
   (for [stream-field (keys stream)]
     ^{:key stream-field}
     [:dl {:class "stream-field"}
      [:dt stream-field]
      [:dd (str (get stream stream-field))]])])

