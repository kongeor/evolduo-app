(ns evolduo-app.views.user
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.music :as music]
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
  (let [action-seed (-> req :session :action-seed)]
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
        [:div.field
         [:label.label "Captcha"]
         [:div.control
          [:input.input {:name "captcha" :type "input" :autocomplete "off" :placeholder "" :value (:captcha signup)}]]
         (when-let [e (:captcha errors)]
           [:p.help.is-danger (first e)])
         [:p.help.is-info (music/describe-action-seed-markup action-seed)]]
        [:div.field.is-grouped
         [:div.control
          [:button.button.is-link {:type "submit"} "Sign up"]]]]])))

(defn account [req & {:keys [user errors]}]
  (let [{:keys [notifications announcements]} (:subscription user)]
    (base-view
      req
      [:div
       [:h2.is-size-3.mb-4 "Account"]
       [:div.field
        [:h3.title.is-5.mb-4 "Personal details"]
        [:label.label "Email"]
        [:div.control
         [:input.input {:name "email" :type "email" :disabled true :autocomplete "off" :value (:email user)}]]]
       [:div
        [:h3.title.is-5.mb-4 "Subscription"]
        [:form {:action "/user/subscription" :method "post"}
         [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
         [:div.field
          [:div.control
           [:label.checkbox
            [:input (merge {:type "checkbox" :name "announcements" :value "true"}
                      (when announcements
                        {:checked "checked"}))]
            " Announcements"]]]
         [:div.field
          [:div.control
           [:label.checkbox
            [:input (merge {:type "checkbox" :name "notifications" :value "true"}
                      (when notifications
                        {:checked "checked"}))]
            " Notifications"]]]
         [:div.field.is-grouped
          [:div.control
           [:button.button.is-link {:type "submit"} "Save"]]]]]
       [:div
        [:h3.title.is-5.mb-4 "Danger zone"]
        [:div "Coming soon"]]

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
            [:button.button.is-link {:type "submit"} "Sign up"]]]]])))

