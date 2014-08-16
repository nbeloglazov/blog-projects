# cljsbin

Code for [Poor Man's Cljsfiddle](http://nbeloglazov.com/2014/08/16/poor-mans-cljsfiddle.html) blog post.

## Usage

Run `lein ring server` and compile/run cljs code in browser. Try following:

```clojure
(ns hello
  (:require [clojure.browser.dom :as dom]))

(defn say-hello []
  (->> "Hello from ClojureScript!"
       (dom/element)
       (dom/append (.-body js/document))))

(.addEventListener js/window "load" say-hello)
```