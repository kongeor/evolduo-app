(ns evolduo-app.response
  (:require [ring.util.response :as resp]
            [hiccup.core :as hiccup]))

(defn render-html [view req data]
  (-> (resp/response (hiccup/html (view req data)))
    (resp/content-type "text/html")))

(defn render-404 []
  (-> (resp/not-found (hiccup/html [:h1 "oops"]))
    (resp/content-type "text/html")))
