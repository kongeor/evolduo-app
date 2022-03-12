(ns evolduo-app.views.user
  (:require [evolduo-app.views.common :refer [base-view]]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn login-view [req]
  (base-view
    req
    [:form {:action "/user/login" :method "post"}
     [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
     [:div.field
      [:label.label "Email"]
      [:div.control
       [:input.input {:name "email" :type "email" :placeholder "user@example.com"}]]]
     [:div.field
      [:label.label "Password"]
      [:div.control
       [:input.input {:name "password" :type "password" :placeholder "Text input"}]]]
     [:div.field.is-grouped
      [:div.control
       [:button.button.is-link {:type "submit"} "Submit"]]
      [:div.control
       [:button.button.is-link.is-light "Cancel"]]]]))

