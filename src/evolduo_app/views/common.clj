(ns evolduo-app.views.common
  (:require [evolduo-app.request :as r]
            [evolduo-app.urls :as u]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn- navbar [req]
  (let [user-id (r/user-id req)
        version (-> req :settings :version)]
    [:nav.navbar.mb-4 {:role "navigation" :aria-label "main navigation"}
     [:div.navbar-brand
      [:a.navbar-item {:href "/"}
      [:img {:src   (u/asset "/img/logo.png" version) :integrity "sha384-LdJLbfV3HMdWBg0Pmg7rWZdFuaLUwMDYAaeYugHoab3d4mQui1nGIW7KKQA4InsN"
             :width "160"}]]
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

(defn base-view
  [req content & {:keys [enable-abc? custom-script body-load-hook notification title]}]
  (let [version (-> req :settings :version)]
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title (str (when title (str title " | ")) "Evolduo")]
      [:link {:rel  "stylesheet"                            ; :integrity "sha384-yJXjG+KvhGtpcF/tiWkrkE8Nq5c9bk2vkMIgy31AEh7KfW3mIOm7B3waZ2ifOFjg"
              :href (u/asset "/css/abcjs-audio.css" version)}]
      [:link {:rel  "stylesheet"                            ; :integrity "sha384-HmYpsz2Aa9Gh3JlkCoh8kUJ2mUKJKTnkyC2Lzt8aLzpPOpnDe8KpFE2xNiBpMDou"
              :href (u/asset "/css/bulma.min.css" version)}]
      [:link {:rel  "stylesheet"                            ; :integrity "sha384-FwI52dlz3G7YiCettbEt+C0IZrLR/fTh50LWsrjMlY/2ViiGZ2qK9Uu3DqtEYOs0"
              :href (u/asset "/css/bulma-slider.min.css" version)}]
      [:link {:rel  "stylesheet"                            ;:integrity "sha384-IKRgH6Fba6Lx3HFJU9vwu7pUO3G9IK230Fq3PP2HwgikTCdBNqoLn+LBkKzRA3MA"
              :href (u/asset "/css/cookie-consent.css" version)}]
      [:link {:rel  "stylesheet"                            ; :integrity "sha384-WKMlC+kdTLo1z3Xee8TLQ9p3j4yEygj3z5vUMMgrLBvHYV8211tAbDXU7XTexQb+"
              :href (u/asset "/css/main.css" version)}]
      [:link {:rel  "icon"                                  ; :integrity "sha384-MjIpSwkFAXQofQSj0BhwOvJOEVUGME9iCScCl0Bk87WNMefLvmy6mZHL8kIEfNLI"
              :type "image/x-icon" :href (u/asset "/img/favicon.ico" version)}]
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
         [:script {:type "text/javascript"                  ; :integrity "sha384-y+euVo1VStesODgUQdlm1ZBcOtxjvarKNiSsJ6LVOV9ube22HaHkONGucRJ6K1se"
                   :src  (u/asset "/js/abcjs-basic-min.js" version)}]
         [:script {:type "text/javascript"                  ; :integrity "sha384-ZubHoDI+2TI8GWxDEkrJloJO6hoQ92yiicxDG77yDOSmdfvo94KRxDZSSAgT73rD"
                   :src  (u/asset "/js/abc-player.js" version)}]])
      [:div
       [:script {:type  "text/javascript" :src (u/asset "/js/chart.min.js" version)}]
       [:script {:type  "text/javascript" :src (u/asset "/js/stats.js" version)}]
       [:script {:defer true                                ; :integrity "sha384-wbyps8iLG8QzJE02viYc/27BtT5HSa11+b5V7QPR1/huVuA8f4LRTNGc82qAIeIZ"
                 :type  "text/javascript" :src (u/asset "/js/bulma-slider.min.js" version)}]
       [:script {:defer true                                ; :integrity "sha384-HEqbPPRxpxIlmcPmqYEe0nwd6BSKQcQOaqYvoLO3lXeA88+6x62211CMsvpRJY0I"
                 :type  "text/javascript" :src (u/asset "/js/cookie-consent.js" version)}]
       [:script {:defer true                                ; :integrity "sha384-rVgq53qSA5OlpuznVuKykYt3uciOwIRZHHMGb1zLPx83WrmHPUvL8YLm2Pa2VGJz"
                 :type  "text/javascript" :src (u/asset "/js/main.js" version)}]]
      ]]))

