(defproject equilibrium "0.1.0-SNAPSHOT"
  :description "Equilibrium"
  :url "http://nbeloglazov.com/2014/09/09/equilibrium.html"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [quil "2.3.0"]
                 [org.clojure/clojurescript "0.0-2268"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler
                        {:output-to "web/js/main.js"
                         :preamble ["processing.js"]
                         :optimizations :simple
                         :pretty-print true}}]})
