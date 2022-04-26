(ns evolduo-app.urls
  (:require [clojure.string :as str]))

(defn token-str [t]
  (cond
    (keyword? t)
    (str (name t))

    :else (str t)))

(defn ->url [& tokens]
  (str "/" (str/join "/" (map token-str tokens))))


(defn url-for [action & {:as params}]
  (case action
    :invitation-form (->url :evolution (:evolution-id params) :invitation :form)
    :invitation-save (->url :evolution (:evolution-id params) :invitation :save)))

(comment
  (url-for :evolution-invite :evolution-id 1))
