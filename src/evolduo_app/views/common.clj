(ns evolduo-app.views.common
  (:require [evolduo-app.request :as r]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn- navbar [req]
  (let [user-id (r/user-id req)]
    [:nav.navbar.mb-4 {:role "navigation" :aria-label "main navigation"}
     [:div.navbar-brand
      [:a.navbar-item {:href "/"}
      [:img {:src "/img/logo.png" :integrity "sha384-LdJLbfV3HMdWBg0Pmg7rWZdFuaLUwMDYAaeYugHoab3d4mQui1nGIW7KKQA4InsN"
             :width "160" }]]
      [:a.navbar-burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarBasicExample"}
       [:span {:aria-hidden "true"}]
       [:span {:aria-hidden "true"}]
       [:span {:aria-hidden "true"}]]]
     [:div#navbarBasicExample.navbar-menu
      [:div.navbar-start
       [:div.navbar-item.has-dropdown.is-hoverable
        [:a.navbar-link "Evolution"]
        [:div.navbar-dropdown
         [:a.navbar-item {:href "/evolution/form"} "New"]
         [:a.navbar-item {:href "/evolution/presets"} "Presets"]]]
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
             [:button.button.is-light {:type "submit"} "Logout"]]]
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
    [:link {:rel "stylesheet" :integrity "sha384-yJXjG+KvhGtpcF/tiWkrkE8Nq5c9bk2vkMIgy31AEh7KfW3mIOm7B3waZ2ifOFjg"
            :href "/css/abcjs-audio.css"}]
    [:link {:rel "stylesheet" :integrity "sha384-HmYpsz2Aa9Gh3JlkCoh8kUJ2mUKJKTnkyC2Lzt8aLzpPOpnDe8KpFE2xNiBpMDou"
            :href "/css/bulma.min.css"}]
    [:link {:rel "stylesheet" :integrity "sha384-FwI52dlz3G7YiCettbEt+C0IZrLR/fTh50LWsrjMlY/2ViiGZ2qK9Uu3DqtEYOs0"
            :href "/css/bulma-slider.min.css"}]
    [:link {:rel "stylesheet" :integrity "sha384-IKRgH6Fba6Lx3HFJU9vwu7pUO3G9IK230Fq3PP2HwgikTCdBNqoLn+LBkKzRA3MA"
            :href "/css/cookie-consent.css"}]
    [:link {:rel "stylesheet" :integrity "sha384-WKMlC+kdTLo1z3Xee8TLQ9p3j4yEygj3z5vUMMgrLBvHYV8211tAbDXU7XTexQb+"
            :href "/css/main.css"}]
    [:link {:rel "icon" :integrity "sha384-MjIpSwkFAXQofQSj0BhwOvJOEVUGME9iCScCl0Bk87WNMefLvmy6mZHL8kIEfNLI"
            :type "image/x-icon" :href "/img/favicon.ico"}]
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
       [:script {:type "text/javascript" :integrity "sha384-y+euVo1VStesODgUQdlm1ZBcOtxjvarKNiSsJ6LVOV9ube22HaHkONGucRJ6K1se"
                 :src  "/js/abcjs-basic-min.js"}]
       [:script {:type "text/javascript" :integrity "sha384-51SfRQezz3RABPJpPMDSGjo+pnXisGQYg+QRf5ViMSrpGnZjXms/0RgIZYlHNE8P"
                 :src  "/js/abc-player.js"}]])
    [:div
     [:script {:defer true :integrity "sha384-wbyps8iLG8QzJE02viYc/27BtT5HSa11+b5V7QPR1/huVuA8f4LRTNGc82qAIeIZ"
               :type "text/javascript" :src "/js/bulma-slider.min.js"}]
     [:script {:defer true :integrity "sha384-HEqbPPRxpxIlmcPmqYEe0nwd6BSKQcQOaqYvoLO3lXeA88+6x62211CMsvpRJY0I"
               :type "text/javascript" :src "/js/cookie-consent.js"}]
     [:script {:defer true :integrity "sha384-rVgq53qSA5OlpuznVuKykYt3uciOwIRZHHMGb1zLPx83WrmHPUvL8YLm2Pa2VGJz"
               :type "text/javascript" :src "/js/main.js"}]]
    ]])

