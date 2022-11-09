(ns evolduo-app.controllers.news
  (:require [evolduo-app.response :as r]
            [evolduo-app.views.news :as view]
            [evolduo-app.model.news :as model]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.request :as req]
            [evolduo-app.model.mail :as mail-model]))

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
  ([req & {:keys [post errors]}]
   (r/render-html view/news-form req {:post post :errors errors})))

(defn news-save
  [{:keys [db] :as req}]
  (let [data           (-> req :params (select-keys [:title :content :action]))
        user-id        (req/user-id req)
        post-id        (some-> req :route-params :id str->int)
        sanitized-data (schemas/decode-and-validate schemas/NewsPost data)]
    (if (:error sanitized-data)
      (->
        (news-form req {:post data :errors (:error sanitized-data)})
        (assoc :flash {:type :danger :message "Oops, something was wrong."}))
      (do
        (let [{:keys [id] :as post} (if post-id
                                      (model/update-news! db user-id post-id (:data sanitized-data))
                                      (model/insert-news! db user-id (:data sanitized-data)))]
          ;; TODO
          (mail-model/insert db {:recipient_id user-id
                                 :type           "announcement"
                                 :data           {:post-id id}})
          (r/redirect (str "/news/" id "/form")
                      :flash {:type :info :message "News entry has been added/updated"}))
        ))))
