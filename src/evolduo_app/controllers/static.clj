(ns evolduo-app.controllers.static
  (:require [clojure.java.io :as io]
            [evolduo-app.response :as r]
            [evolduo-app.views.common :refer [base-view]]
            [hickory.core :as hickory]))

(defn- doc-as-hiccup [doc]
  (hickory/as-hiccup (hickory/parse (slurp (io/resource doc)))))

(defn render-static-page [req page]
  (let [doc (doc-as-hiccup page)]
    (r/render-html (fn [req] (base-view req doc)) req)))

;; TODO memoize for prod

(defn samples [req]
  (render-static-page req "doc/samples.html"))

(defn contact [req]
  (render-static-page req "doc/contact.html"))

(defn privacy-policy
  [req]
  (render-static-page req "doc/privacy-policy.html"))

(defn terms-of-service
  [req]
  (render-static-page req "doc/terms-and-conditions.html"))
