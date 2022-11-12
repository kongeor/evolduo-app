(ns evolduo-app.response
  (:require [ring.util.response :as resp]
            [hiccup.page :as page]))

(defn- html5-page [contents]
  (page/html5 {:lang "en"} (apply list contents)))

(comment
  (html5-page [[:head] [:body [:p "yo"]]]))

(defn render-html
  ([view req]
   (-> (resp/response (html5-page (view req)))
     (resp/content-type "text/html")))
  ([view req data]
   (-> (resp/response (html5-page (view req data)))
     (resp/content-type "text/html"))))

(defn render-404 []
  (-> (resp/not-found (html5-page [:h1 "oops"]))
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

(comment
  (update-in {:a 1} [:ab] #(if % (inc %) 0)))