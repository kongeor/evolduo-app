(ns evolduo-app.views.home
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]
            [evolduo-app.urls :as u]))


(defn home [req {:keys [user-evolutions public-evolutions invited-evolutions]}]
  (base-view
    req
    [:div
     [:p.mb-4 "Evolduo is platform for collaborative musical synthesis using evolutionary algorithms.
               It is open source and free to use."]
     [:p.mb-4 "Not sure where to start?
               Check the " [:a {:target "_blank" :href "https://youtu.be/G9Iwgwp_GD4"} "quick start video tutorial"] "."]
     (when (seq user-evolutions)
       [:div.mb-4
        [:h2.is-size-4.mb-4 "My evolutions"]
        [:p.mb-4 "Evolutions created by you."]
        (comps/evolution-table user-evolutions)
        [:a {:href (u/url-for :evolution-search {:query {:type "my"}})} "more"]
        [:hr]])

     (when (seq invited-evolutions)
       [:div.mb-4
        [:h2.is-size-4.mb-4 "Friends' evolutions"]
        [:p.mb-4 "Evolutions you have been invited to."]
        (comps/evolution-table invited-evolutions)
        [:a {:href (u/url-for :evolution-search {:query {:type "friends"}})} "more"]
        [:hr]])

     (when (seq public-evolutions)
       [:div.mb-4
        [:h2.is-size-4.mb-4 "Public evolutions"]
        [:p.mb-4 "Evolutions created by others that are marked as public."]
        (comps/evolution-table public-evolutions)
        [:a {:href (u/url-for :evolution-search {:query {:type "public"}})} "more"]
        [:hr]])
     ]
    :title "Home"))
