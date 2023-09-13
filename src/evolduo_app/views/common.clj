(ns evolduo-app.views.common
  (:require [evolduo-app.request :as r]
            [evolduo-app.urls :as u]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn- navbar [req]
  (let [user-id (r/user-id req)
        version (-> req :settings :version)]
    [:nav.navbar.mb-4 {:role "navigation" :aria-label "main"}
     [:div.navbar-brand
      [:a.navbar-item {:href "/"}
      [:img {:src   (u/asset "/img/logo.png" version) :width "160" :alt "evolduo logo"}]]
      [:a.navbar-burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarBasicExample"}
       [:span {:aria-hidden "true"}]
       [:span {:aria-hidden "true"}]
       [:span {:aria-hidden "true"}]]]
     [:div#navbarBasicExample.navbar-menu
      [:div.navbar-start
       [:div.navbar-item.has-dropdown.is-hoverable
        [:a.navbar-item {:href "/evolution/form"} "New Evolution"]
        [:a.navbar-item {:href "/evolution/presets"} "Presets"]]
       [:a.navbar-item {:href "/evolution/library?type=public"} "Library"]
       [:a.navbar-item {:href "/playground"} "Playground"]
       [:a.navbar-item {:href "/news"} "News"]
       [:a.navbar-item {:href "/samples"} "Samples"]
       [:a.navbar-item {:href "/contact"} "Contact"]]
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
             [:button.button.is-light {:type "submit"} "Logout"]]]
           [:a.button.is-light {:href "/user/login"} "Login"])]]]]]))

(defn- notification-div [data]
  (when data
    [:div {:class (str "notification " (str "is-" (name (:type data))))}
     (:message data)]))

(defn- silent-parse-boolean
  "Don't throw"
  [v]
  (try
    (parse-boolean v)
    (catch Exception _)))

(silent-parse-boolean 1)

(defn base-view
  [req content & {:keys [enable-abc? custom-script body-load-hook notification title]}]
  (let [version (-> req :settings :version)
        tracking-allowed? (some-> req :cookies (get "cookie-consent-tracking-allowed") :value silent-parse-boolean)]
    [[:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:meta {:name "description" :content "Evolduo is platform for collaborative musical synthesis using evolutionary algorithms"}]
      [:title (str (when title (str title " | ")) "Evolduo")]
      [:link {:rel  "stylesheet" :href (u/asset "/css/abcjs-audio.css" version)}]
      [:link {:rel  "stylesheet" :href (u/asset "/css/bulma.min.css" version)}]
      [:link {:rel  "stylesheet" :href (u/asset "/css/bulma-slider.min.css" version)}]
      [:link {:rel  "stylesheet" :href (u/asset "/css/cookie-consent.css" version)}]
      [:link {:rel  "stylesheet" :href (u/asset "/css/main.css" version)}]
      [:link {:rel  "icon" :type "image/x-icon" :href (u/asset "/img/favicon.ico" version)}]
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
        [:p
         [:a {:href "https://github.com/kongeor/evolduo-app"} "Source Code"]
         [:span " | "]
         [:a {:href "/privacy-policy"} "Privacy Policy"]
         [:span " | "]
         [:a {:href "/terms-of-service"} "Terms of Service"]
         [:span " | "]
         [:a {:href "https://github.com/kongeor/evolduo-app#support"} "Support"]
         [:span " | "]
         [:a {:href "/contact"} "Contact"]
         [:span " | "]
         [:a {:href "/stats"} "Stats"]
         ]
        [:p "Built with ❤️ using " [:a {:href "https://clojure.org/"} "Clojure"]]
        [:p (str (-> req :settings :version))]]]
      (when custom-script
        [:script {:type "text/javascript"}
         custom-script])
      (when enable-abc?
        [:div
         [:script {:type "text/javascript" :src  (u/asset "/js/abcjs-basic-min.js" version)}]
         [:script {:type "text/javascript" :src  (u/asset "/js/abc-player.js" version)}]])
      [:div
       [:script {:src (u/asset "/js/chart.min.js" version)}]
       [:script {:src (u/asset "/js/stats.js" version)}]
       [:script {:defer true :src (u/asset "/js/bulma-slider.min.js" version)}]
       [:script {:defer true :src (u/asset "/js/cookie-consent.js" version)}]
       [:script {:defer true :src (u/asset "/js/main.js" version)}]
       (when tracking-allowed?
         (list
           [:noscript
            [:img {:src "https://stats.cons.gr/ingress/b6fd35c4-497c-40b3-ae3b-1ec073719285/pixel.gif"}]]
           [:script {:defer "true" :src "https://stats.cons.gr/ingress/b6fd35c4-497c-40b3-ae3b-1ec073719285/script.js"}]))]
      ]]))

