(ns evolduo-app.urls
  (:require [clojure.string :as str]
            [ring.util.codec :as codec]))

(defn token-str [t]
  (cond
    (keyword? t)
    (str (name t))

    :else (str t)))

(defn- ->url [& tokens]
  (str "/" (str/join "/" (map token-str tokens))))


(defn url-for [action & {:as params}]
  (case action
    :explorer (str (->url :explorer) "?" (codec/form-encode (:query params)))
    :invitation-form (->url :evolution (:evolution-id params) :invitation :form)
    :invitation-save (->url :evolution (:evolution-id params) :invitation :save)))

(comment
  (url-for :explorer :query {:a 1})
  (url-for :evolution-invite :evolution-id 1))
