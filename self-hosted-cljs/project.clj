(defproject self-hosted-cljs "0.1.0-SNAPSHOT"
  :description "Simple example of self-hosted cljs"
  :url "http://nbeloglazov.com/2016/03/05/getting-started-with-self-hosted-cljs-part-1.html"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]]

  :plugins [[lein-cljsbuild "1.1.2"]]
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds [{:source-paths ["src"]
             :compiler
             {:output-to "js/main.js"
              :output-dir "out"
              :main "my.main"
              :optimizations :none
              :pretty-print true}}]})
