(ns electron.state)

(def main-window (atom nil))
(def tasks       (atom (sorted-map)))
