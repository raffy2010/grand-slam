(defproject grand-slam "0.1.0"
  :source-paths ["src" "ui_src" "electron_src"]
  :description "a video editor based on FFmpeg, Electron, ClojureScript and Material design."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [figwheel "0.5.8"]
                 [reagent "0.6.0" :exclusions [org.clojure/tools.render
                                               cljsjs/react
                                               cljsjs/react-dom]]
                 [cljs-react-material-ui "0.2.38"]
                 [ring/ring-core "1.5.0"]
                 [binaryage/devtools "0.9.2"]
                 [figwheel-sidecar "0.5.8"]]
  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.8"]
            [deraen/lein-sass4clj "0.3.1"]
            [lein-doo "0.1.7"]
            [cider/cider-nrepl "0.14.0"]]

  :clean-targets ^{:protect false} ["resources/main.js"
                                    "resources/public/js/ui-core.js"
                                    "resources/public/js/ui-core.js.map"
                                    "resources/public/js/ui-out"]
  :sass {:target-path "resources/public/css"
         :source-paths ["resources/assets/styles"]}
  :cljsbuild
  {:builds
   [{:source-paths ["electron_src"]
     :id "electron-dev"
     :compiler {:output-to "resources/main.js"
                :output-dir "resources/public/js/electron-dev"
                :target :nodejs
                :optimizations :simple
                :pretty-print true
                :cache-analysis true}}
    {:source-paths ["ui_src" "dev_src"]
     :id "frontend-dev"
     :compiler {:preloads [devtools.preload]
                :output-to "resources/public/js/ui-core.js"
                :output-dir "resources/public/js/ui-out"
                :source-map true
                :asset-path "js/ui-out"
                :optimizations :none
                :cache-analysis true
                :main "dev.core"}}
    {:source-paths ["electron_src"]
     :id "electron-release"
     :compiler {:output-to "resources/main.js"
                :output-dir "resources/public/js/electron-release"
                :optimizations :simple
                :pretty-print true
                :cache-analysis true}}
    {:source-paths ["ui_src"]
     :id "frontend-release"
     :compiler {:output-to "resources/public/js/ui-core.js"
                :output-dir "resources/public/js/ui-release-out"
                :source-map "resources/public/js/ui-core.js.map"
                :optimizations :simple
                :cache-analysis true
                :main "ui.core"}}]}
  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]
             :ring-handler tools.figwheel-middleware/app
             :server-port 3449}
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
