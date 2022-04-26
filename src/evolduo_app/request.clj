(ns evolduo-app.request)

(defn user-id [request]
  (some-> request
    :session
    :user/id))