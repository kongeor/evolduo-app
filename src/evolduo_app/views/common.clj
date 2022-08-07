(ns evolduo-app.views.common
  (:require [evolduo-app.request :as r]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn- navbar [req]
  (let [user-id (r/user-id req)]
    [:nav.navbar.mb-4 {:role "navigation" :aria-label "main navigation"}
     [:div.navbar-brand
      [:a.navbar-item {:href "/"}
       [:img {:src "/img/logo.png" :width "112" :height "28"}]]
      [:a.navbar-burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarBasicExample"}
       [:span {:aria-hidden "true"}]
       [:span {:aria-hidden "true"}]
       [:span {:aria-hidden "true"}]]]
     [:div#navbarBasicExample.navbar-menu
      [:div.navbar-start
       [:a.navbar-item {:href "/evolution/form"} "New Evolution"]
       [:a.navbar-item {:href "/evolution/search?type=public"} "Search"]
       [:a.navbar-item {:href "/explorer"} "Explorer"]]
      [:div.navbar-end
       [:div.navbar-item
        [:div.buttons
         (if user-id
           [:a.button.is-primary {:href "/user/account"} [:strong "Account"]]

           [:a.button.is-primary {:href "/user/signup"} [:strong "Sign up"]])
         (if user-id
           [:div
            [:form.mb-0 {:action "/user/logout" :method "post"}
             [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
             [:button.button.is-light {:type "submit"} "Log out"]]]
           [:a.button.is-light {:href "/user/login"} "Login"])]]]]]))

(defn- notification-div [data]
  (when data
    [:div {:class (str "notification " (str "is-" (name (:type data))))}
     (:message data)]))

(defn base-view
  [req content & {:keys [enable-abc? custom-script body-load-hook notification]}]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Evolduo"]
    [:link {:rel "stylesheet" :href "/css/abcjs-audio.css"}]
    [:link {:rel "stylesheet" :href "/css/bulma.min.css"}]
    [:link {:rel "stylesheet" :href "/css/cookie-consent.css"}]
    ]
   [:body (when (and body-load-hook enable-abc?) {:onload body-load-hook})
    [:section.section
     [:div.container
      (navbar req)
      (notification-div (:flash req))
      (notification-div notification)
      content]]
    [:footer.footer
     [:div.content.has-text-centered
      [:p [:a {:href "https://github.com/kongeor/evolduo-app"} "Source"]]
      [:p (str (-> req :settings :version))]]]
    (when custom-script
      [:script {:type "text/javascript"}
       custom-script])
    (when enable-abc?
      [:div
       [:script {:type "text/javascript"
                 :src  "https://cdn.jsdelivr.net/npm/abcjs@6.0.2/dist/abcjs-basic-min.js"}]
       [:script {:type "text/javascript"
                 :src  "/js/abc-player.js"}]])
    [:div
     [:script {:defer true :type "text/javascript" :src "/js/cookie-consent.js"}]
     [:script {:defer true :type "text/javascript" :src "/js/main.js"}]]
    ]])

