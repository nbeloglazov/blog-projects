(defproject cljsbin "0.1.0-SNAPSHOT"
  :description "Poor Man's Cljsfiddle"
  :url "http://nbeloglazov.com/2014/08/16/poor-mans-cljsfiddle.html"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [ring "1.3.0"]
                 [ring/ring-json "0.3.1"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [quil "2.2.2-SNAPSHOT"]
                 [me.raynes/fs "1.4.6"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler cljsbin/handler})
