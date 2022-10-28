(ns evolduo-app.urls
  (:require [clojure.string :as str]
            [ring.util.codec :as codec]))

(defn asset
  "Browser cache busting"
  [path version]
  (str path "?v=" version))

(defn token-str [t]
  (cond
    (keyword? t)
    (str (name t))

    :else (str t)))

(defn- ->url [& tokens]
  (str "/" (str/join "/" (map token-str tokens))))

;; TODO just terrible
(defn- ->url-with-hash [hash & tokens]
  (str "/" (str/join "/" (map token-str tokens)) "#" hash))

(defn url-for [action & {:as params}]
  (case action
    :evolution-search (str (->url :evolution :library) "?" (codec/form-encode (:query params)))
    :evolution-form (->url :evolution :form)
    :evolution-detail (->url :evolution (:evolution-id params))
    :iteration-detail (->url-with-hash (str "abc_" (:chromosome-id params))
                        :evolution (:evolution-id params) :iteration (:iteration-num params))
    :playground (str (->url :playground) "?" (codec/form-encode (:query params)))
    :invitation-form (->url :evolution (:evolution-id params) :invitation :form)
    :invitation-save (->url :evolution (:evolution-id params) :invitation :save)))

(comment
  (url-for :playground :query {:a 1})
  (url-for :evolution-invite :evolution-id 1))
