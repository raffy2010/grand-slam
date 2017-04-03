(ns electron.menu
 (:require-macros [cljs.core.async.macros :refer [go]])
 (:require [electron.state :refer [main-window]]
           [electron.ffmpeg :refer [probe-video respond-probe]]
           [cljs.core.async :as async :refer [<! >! put! chan alts! timeout]]))

(defonce electron       (js/require "electron"))

(defonce dialog       (.-dialog electron))

(defn handle-file-select
  [file-list]
  (go
    (when-let [file (first file-list)]
      (->> @main-window
           .-webContents
           (respond-probe (<! (probe-video file)))))))

(defn open-file
  "open video files"
  []
  (.showOpenDialog dialog @main-window
                          {:properties ["openFile" "multiSelections"]}
                          handle-file-select))

(defn get-menu-template
  []
  [{:label "Grand-slam"
    :submenu [{:role "about"}
              {:type "separator"}
              {:label "Preferences"
               :accelerator "Cmd+"}
              {:type "separator"}
              {:role "services"
               :submenu []}
              {:type "separator"}
              {:role "hide"}
              {:role "hideothers"}
              {:role "unhide"}
              {:type "separator"}
              {:role "quit"}]}
   {:label "File"
    :submenu [{:label "Open File"
               :accelerator "CmdOrCtrl+o"
               :click open-file}]}
   {:label "View"
    :submenu [{:label "Reload"
               :accelerator "Cmd+r"
               :click #(.reload @main-window)}
              {:label "Devtool"
               :accelerator "Cmd+Alt+i"
               :click #(.toggleDevTools @main-window)}]}])


(defonce app-menu (.buildFromTemplate (.-Menu electron)
                                      (clj->js (get-menu-template))))

(defn init-menu
  "initialize app menu"
  []
  (.setApplicationMenu (.-Menu electron)
                       app-menu))


