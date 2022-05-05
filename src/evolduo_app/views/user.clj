(ns evolduo-app.views.user
  (:require [evolduo-app.views.common :refer [base-view]]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn login-form [req]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "Login"]
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
        [:button.button.is-link {:type "submit"} "Login"]]
       [:div.control
        [:button.button.is-link.is-light "Cancel"]]]]]))

(defn signup-form [req & {:keys [signup errors]}]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "Sign up"]
     [:form {:action "/user/signup" :method "post"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:label.label "Email"]
       [:div.control
        [:input.input {:name "email" :type "email" :autocomplete "off" :placeholder "user@example.com" :value (:email signup)}]]
       (when-let [e (:email errors)]
         [:p.help.is-danger (first e)])]
      [:div.field
       [:label.label "Password"]
       [:div.control
        [:input.input {:name "password" :type "password" :autocomplete "off" :placeholder ""}]]
       (when-let [e (:password errors)]
         [:p.help.is-danger (first e)])]
      [:div.field
       [:label.label "Password Confirmation"]
       [:div.control
        [:input.input {:name "password_confirmation" :type "password" :autocomplete "off" :placeholder ""}]]
       (when-let [e (:password_confirmation errors)]
         [:p.help.is-danger (first e)])]
      [:div.field.is-grouped
       [:div.control
        [:button.button.is-link {:type "submit"} "Sign up"]]]]]))

(defn account [req & {:keys [user errors]}]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "Account"]
     [:div.field
      [:label.label "Email"]
      [:div.control
       [:input.input {:name "email" :type "email" :disabled true :autocomplete "off" :value (:email user)}]]]
     #_[:form {:action "/user/signup" :method "post"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:label.label "Email"]
       [:div.control
        [:input.input {:name "email" :type "email" :autocomplete "off" :placeholder "user@example.com" :value (:email signup)}]]
       (when-let [e (:email errors)]
         [:p.help.is-danger (first e)])]
      [:div.field
       [:label.label "Password"]
       [:div.control
        [:input.input {:name "password" :type "password" :autocomplete "off" :placeholder ""}]]
       (when-let [e (:password errors)]
         [:p.help.is-danger (first e)])]
      [:div.field
       [:label.label "Password Confirmation"]
       [:div.control
        [:input.input {:name "password_confirmation" :type "password" :autocomplete "off" :placeholder ""}]]
       (when-let [e (:password_confirmation errors)]
         [:p.help.is-danger (first e)])]
      [:div.field.is-grouped
       [:div.control
        [:button.button.is-link {:type "submit"} "Sign up"]]]]]))

