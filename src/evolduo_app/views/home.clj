(ns evolduo-app.views.home
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]
            [evolduo-app.urls :as u]))


(defn home [req {:keys [user-evolutions public-evolutions invited-evolutions]}]
  (base-view
    req
    [:div
     (when (seq user-evolutions)
       [:div.mb-4
        [:h2.is-size-4 "Your evolutions"]
        (comps/evolution-table user-evolutions)
        [:a {:href (u/url-for :evolution-search {:query {:type "my"}})} "more"]
        [:hr]])

     (when (seq invited-evolutions)
       [:div.mb-4
        [:h2.is-size-4 "Invited evolutions"]
        (comps/evolution-table invited-evolutions)
        [:a {:href (u/url-for :evolution-search {:query {:type "invited"}})} "more"]
        [:hr]])

     (when (seq public-evolutions)
       [:div.mb-4
        [:h2.is-size-4 "Public evolutions"]
        (comps/evolution-table public-evolutions)
        [:a {:href (u/url-for :evolution-search {:query {:type "public"}})} "more"]
        [:hr]])
     ]))
