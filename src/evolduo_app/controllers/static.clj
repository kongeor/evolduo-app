(ns evolduo-app.controllers.static
  (:require [clojure.java.io :as io]
            [evolduo-app.response :as r]
            [evolduo-app.views.common :refer [base-view]]
            [hickory.core :as hickory]))

(defn- doc-as-hiccup [doc]
  (hickory/as-hiccup (hickory/parse (slurp (io/resource doc)))))

;; TODO memoize for prod

(defn privacy-policy
  [req]
  (let [doc (doc-as-hiccup "doc/privacy-policy.html")]
    (r/render-html (fn [req] (base-view req doc)) req)))

(defn terms-of-service
  [req]
  (let [doc (doc-as-hiccup "doc/terms-and-conditions.html")]
    (r/render-html (fn [req] (base-view req doc)) req)))
