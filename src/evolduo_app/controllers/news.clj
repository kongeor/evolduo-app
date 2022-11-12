(ns evolduo-app.controllers.news
  (:require [evolduo-app.model.news :as model]
            [evolduo-app.request :as req]
            [evolduo-app.response :as r]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.views.news :as view]))

(defn news-list
  [req]
  (r/render-html view/news-list req))

(defn str->int [s]
  (Integer/parseInt s))

(defn news-form
  ([{:keys [db] :as req}]
   (if-let [id (some-> req :route-params :id str->int)]
     (let [post (model/find-edit-post db id)]
       (news-form req :post post))
     (news-form req {})))
  ([{:keys [is-admin?] :as req} & {:keys [post errors]}]
   (if is-admin?
     (r/render-html view/news-form req {:post post :errors errors})
     (r/redirect "/"
                 :flash {:type :danger :message "No, sorry ..."}))
     ))

(defn news-save
  [{:keys [db is-admin?] :as req}]
  (let [data           (-> req :params (select-keys [:title :content :action]))
        user-id        (req/user-id req)
        post-id        (some-> req :route-params :id str->int)
        sanitized-data (schemas/decode-and-validate schemas/NewsPost data)]
    (cond
      (:error sanitized-data)
      (->
        (news-form req {:post data :errors (:error sanitized-data)})
        (assoc :flash {:type :danger :message "Oops, something was wrong."}))

      (not is-admin?)
      (r/redirect "/"
                 :flash {:type :danger :message "No, sorry ..."})

      :else
      (do
        (let [{:keys [id] :as post} (if post-id
                                      (model/update-news! db user-id post-id (:data sanitized-data))
                                      (model/insert-news! db user-id (:data sanitized-data)))]
          (r/redirect (str "/news/" id "/form")
                      :flash {:type :info :message "News entry has been added/updated"}))
        ))))
