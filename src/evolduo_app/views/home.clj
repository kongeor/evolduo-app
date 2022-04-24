(ns evolduo-app.views.home
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]))


(defn home [req {:keys [user-evolutions]}]
  (base-view
    req
    [:div
     [:h2.is-size-4 "Your evolutions"]
     (comps/evolution-table user-evolutions)]))
