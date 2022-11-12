(ns evolduo-app.views.news
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.schemas :as schema]
            [evolduo-app.model.news :as model]
            [ring.middleware.anti-forgery :as anti-forgery])
  (:import (java.text SimpleDateFormat)))

(def ^:private date-formatter (SimpleDateFormat. "dd/MM/yyyy HH:mm"))

(defn- format-date [d]
  (.format date-formatter d))

(comment
  (format-date (java.util.Date.)))

;; TODO why is- ?
(defn news-list [{:keys [settings db is-admin?] :as req}]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "News"]
     (when is-admin?
       [:a.button.is-primary.mb-4 {:href "/news/form"} "New Post"])
     [:div.content
      (for [post (model/fetch-news db (if is-admin? :all {:status "published"}))]
        [:div.mb-4
         [:h3 (:title post)]
         [:p (str (if (:updated_at post) "Updated on: " "Published on: ")
                  (format-date (or (:updated_at post) (:created_at post))))]
         (when is-admin?
           [:div.mb-4
            [:span.tag.is-info (:status post)]])
         [:div (:content_html post)]
         (when is-admin?
           [:a {:href (str "/news/" (:id post) "/form")} "edit"])
         [:hr.mb-4]])]]
    :title "News"
    ))

(defn news-form [req & {:keys [post errors]}]
  (let [action (if (:id post)
                 (str "/news/" (:id post) "/save")
                 "/news/save")]
    (base-view
      req
      [:div
       [:h2.is-size-3.mb-4 "Add new post"]
       [:form {:action action :method "post"}
        [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
        [:div.field
         [:label.label "Title"]
         [:div.control
          [:input.input {:name "title" :type "text" :placeholder "Text input" :value (:title post)}]]]
        [:div.field
         [:label.label "Content (md)"]
         [:div.control
          [:textarea.textarea {:name "content" :placeholder "Textarea"} (:content post)]]]
        [:div.field
         [:label.label "Action"]
         [:div.control
          [:div.select
           [:select {:name "action"}
            (for [[value label] schema/post-actions]
              [:option {:value value} label])

            ]]]]
        [:div.field.is-grouped
         [:div.control
          [:button.button.is-link "Submit"]]]]
       ]
      :title "New Post"
      )))