(ns evolduo-app.views.user
  (:require [evolduo-app.image :as image]
            [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.model.captcha :as captcha]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn login-form [req & {:keys [login notification]}]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "Login"]
     [:form {:action "/user/login" :method "post"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:label.label "Email"]
       [:div.control
        [:input.input {:name "email" :type "email" :placeholder "user@example.com" :value (:email login)}]]]
      [:div.field
       [:label.label "Password"]
       [:div.control
        [:input.input {:name "password" :type "password" :placeholder "Text input"}]]]
      [:div.field.is-grouped
       [:div.control
        [:button.button.is-link {:type "submit"} "Login"]]]]]
    :notification notification))

(defn signup-form [req & {:keys [captcha signup errors]}]
  (base-view
    req
    [:div
     [:h2.is-size-3.mb-4 "Sign up"]
     [:form {:action "/user/signup" :method "post"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:label.label {:for "email"} "Email"]
       [:div.control
        [:input.input {:id "email" :name "email" :type "email" :required "required" :autocomplete "off" :placeholder "user@example.com" :value (:email signup)}]]
       (when-let [e (:email errors)]
         [:p.help.is-danger (first e)])]
      [:div.field
       [:label.label {:for "password"} "Password"]
       [:div.control
        [:input.input {:id "password" :name "password" :type "password" :required "required" :autocomplete "off" :placeholder ""}]]
       (when-let [e (:password errors)]
         [:p.help.is-danger (first e)])
       [:p.help.is-info "Password should have at minimum eight characters, at least one uppercase letter, one lowercase letter and one number"]]
      [:div.field
       [:label.label {:for "password-confirmation"} "Password Confirmation"]
       [:div.control
        [:input.input {:id "password-confirmation" :name "password_confirmation" :type "password" :required "required" :autocomplete "off" :placeholder ""}]]
       (when-let [e (:password_confirmation errors)]
         [:p.help.is-danger (first e)])]
      [:div.field
       [:label.label {:for "captcha"} "Captcha"]
       [:img {:src (str "data:image/png;base64, " (image/captcha-text->base64 captcha))}]
       [:div.control
        [:input.input {:id "captcha" :name "captcha" :type "input" :required "required" :autocomplete "off" :placeholder "" :value (:captcha signup)}]]
       (when-let [e (:captcha errors)]
         [:p.help.is-danger (first e)])
       [:p.help.is-info.mb-2 "You are not a robot, are you? Please enter the number you see above. Use the audio player below to hear the captcha value"]
       [:audio {:controls "true" :src  (str "data:audio/wav;base64, " (captcha/captcha-audio->base64 captcha))} "Your browser does not support the"
        [:code "audio"] "element."]]
      [:div.field.is-grouped
       [:div.control
        [:button.button.is-link {:type "submit"} "Sign up"]]]]]))

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
       [:div.mb-5
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
        [:p.mb-4 "Account deletion is an irreversible action. Your email will be changed to a random value and your content will be hidden."]
        [:p.mb-4 "You will need to type " [:code "I am awesome!"] " in the input field below to confirm this action."]
        [:form {:action "/user/delete" :method "post"}
         [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
         [:div.field
          [:label.label "Confirmation"]
          [:div.control
           [:input.input {:name "confirmation" :type "input" :autocomplete "off"}]]]
         [:div.field.is-grouped
          [:div.control
           [:button.button.is-danger {:type "submit"} "Delete my account"]]]]] ])))

