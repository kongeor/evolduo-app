(ns evolduo-app.response
  (:require [ring.util.response :as resp]
            [evolduo-app.music :as music]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(defn render-html
  ([view req]
   (-> (resp/response (page/html5 (view req)))
     (resp/content-type "text/html")))
  ([view req data]
   (-> (resp/response (page/html5 (view req data)))
     (resp/content-type "text/html"))))

(defn render-404 []
  (-> (resp/not-found (page/html5 [:h1 "oops"]))
    (resp/content-type "text/html")))

(defn redirect [url & {:keys [flash]}]
  (cond-> (resp/redirect url)
    flash
    (assoc
      :flash flash)))

(defn logout [& {:keys [message] :or {message "I am awesome!"}}]
  (->
    (redirect "/"
      :flash {:type :info :message message})
    (assoc :session nil)))

(defn set-sensitive-actions-seed [resp]
  (update-in resp [:session :action-seed] #(if %
                                             %
                                             (music/generate-action-seed))))

(comment
  (update-in {:a 1} [:ab] #(if % (inc %) 0)))