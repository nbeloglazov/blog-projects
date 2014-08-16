(ns cljsbin
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [files]]
            [ring.middleware.json :as json]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as resp]
            [hiccup.page :refer [html5]]
            [cljs.closure :as cljs]
            [me.raynes.fs :as fs]))

; Create temp dir where cljs will be compiled.
; It is used to speed up compilation: clojurescript compiler stores
; intermediate results there, like translated to js cljs.core and
; clojure.* namespaces. The directory is optional.
(def cljs-compilation-dir (fs/temp-dir "cljs-compilation"))

(defn compile-cljs [source]
  (let [; Clojurescript compiler prefers to work with files as
        ; input/outputs so we create temp files for the source
        ; and compiled output
        source-file (fs/temp-file "cljs-source")
        compiled-file (fs/temp-file "cljs-compiled")]

    ; Write source into the temp file.
    (spit source-file source)

    ; Compile source using :simple level of optimization.
    (cljs/build source-file
                {:optimizations :simple
                 :output-to (.getAbsolutePath compiled-file)
                 :output-dir (.getAbsolutePath cljs-compilation-dir)
                 :pretty-print true})

    ; Read compiled output and cleanup temp files.
    (let [compiled (slurp compiled-file)]
      (fs/delete source-file)
      (fs/delete compiled-file)
      compiled)))

; Save all snippets in an atom. We could use db,
; but we're doing poor's man cljsfiddle after all.
; It is a map id -> js.
(def snippets (atom {}))

; Unique id generator.
(let [id (atom 0)]
  (defn next-id []
    (str (swap! id inc))))

; Implementation of "/create".
; Compile and store source, return snippet id.
(defn create-snippet [source]
  (let [id (next-id)
        js (compile-cljs source)]
    (swap! snippets assoc id js)
    (resp/response {:id id})))

; Create response for "/js/ID"
(defn snippet-js [id]
  (-> (@snippets id "")
      (resp/response)
      (resp/content-type "application/javascript")))

; Create response for "/html/ID"
(defn snippet-html [id]
  (-> (list [:head
             [:title (str "Snippet " id)]
             [:script {:src (str "/js/" id)}]]
            [:body])
      html5))

(defroutes app
  (POST "/create" req (-> req :body :source create-snippet))
  (GET "/html/:id" [id] (snippet-html id))
  (GET "/js/:id" [id] (snippet-js id))
  ; Serve index.html as initial page when user requests
  ; http://localhost:8080/
  (GET "/" [] (slurp "public/index.html"))
  ; Serve static files. By default 'public' directory is used.
  ; Example: public/script.js served when user requests
  ; http://localhost:8080/script.js
  (files "/"))

; Use ring middleware to decode/encode json requests/response.
(def handler
  (-> app
      (json/wrap-json-body {:keywords? true})
      json/wrap-json-response))

(comment
  (def server (run-jetty handler {:port 8080 :join? false}))
  (.stop server))
