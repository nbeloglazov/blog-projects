(ns my.main
  (:require [cljs.js :as cjs]
            [cljs.pprint :refer [pprint]]))

(enable-console-print!)

(def state (cjs/empty-state))

(cjs/eval-str state
              "(.log js/console \"Hello, world\")"
              "bla"
              {:eval cjs/js-eval}
              identity)
