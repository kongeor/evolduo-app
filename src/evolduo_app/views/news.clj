(ns evolduo-app.views.news
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.schemas :as schema]
            [evolduo-app.model.news :as model]
            [clojure.data.json :as json]
            [clojure.core.memoize :as memo]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn news-list [{:keys [settings db] :as req}]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "News"]
     [:div
      (for [post (model/fetch-news db)]
        [:div
         [:h1 (:title post)]
         [:div (:content_md post)]
         [:a {:href (str "/news/" (:id post) "/form")} "edit"]])]]
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