(ns ui.core
 (:require [cljsjs.material-ui]
           [cljs-react-material-ui.core :refer [get-mui-theme color]]
           [cljs-react-material-ui.reagent :as ui]
           [cljs-react-material-ui.icons :as ic]
           [reagent.core :as reagent]
           [ui.component.drag-and-drop :refer [drag-and-drop]]
           [ui.component.msg-tips :refer [msg-tips]]))

(enable-console-print!)

(defn root-component []
  [ui/mui-theme-provider
   {:mui-theme (get-mui-theme)}
   [:div {:class "ui-root"}
    [msg-tips]
    [drag-and-drop]]])

(reagent/render
  [root-component {:class "app-wrap"}]
  (js/document.getElementById "app-container"))
