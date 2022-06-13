(ns evolduo-app.middleware
  (:require [ring.middleware.anti-forgery :as anti-forgery]))

(defn anti-forgery->captcha-code []
  (subs (str (abs (hash anti-forgery/*anti-forgery-token*))) 0 6))

