(ns evolduo-app.views.home
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]))


(defn home [req {:keys [user-evolutions public-evolutions invited-evolutions]}]
  (base-view
    req
    [:div
     (when user-evolutions
       [:div.mb-4
        [:h2.is-size-4 "Your evolutions"]
        (comps/evolution-table user-evolutions)])

     (when invited-evolutions
       [:div.mb-4
        [:h2.is-size-4 "Invited evolutions"]
        (comps/evolution-table invited-evolutions)])

     [:h2.is-size-4 "Public evolutions"]
     (comps/evolution-table public-evolutions)
     ]))
