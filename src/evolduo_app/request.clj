(ns evolduo-app.request)

(defn user-id [request]
  (some-> request
    :session
    :user/id))

(defn get-x-forwarded-for-header [request]
  (-> request
    :headers
    (get "x-forwarded-for")))