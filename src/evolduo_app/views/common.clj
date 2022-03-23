(ns evolduo-app.views.common)

(defn- navbar [req]
  [:nav.navbar {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    [:a.navbar-item {:href "/"}
     [:img {:src "https://bulma.io/images/bulma-logo.png" :width "112" :height "28"}]]
    [:a.navbar-burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarBasicExample"}
     [:span {:aria-hidden "true"}]
     [:span {:aria-hidden "true"}]
     [:span {:aria-hidden "true"}]]]
   [:div#navbarBasicExample.navbar-menu
    [:div.navbar-start
     [:a.navbar-item {:href "/evolution/form"} "New Evolution"]
     [:a.navbar-item {:href "/evolution/list"} "Evolutions"]
     [:a.navbar-item {:href "/explorer"} "Explorer"]]
    [:div.navbar-end
     [:div.navbar-item
      [:div.buttons
       [:a.button.is-primary
        [:strong "Sign up"]]
       (do
         (println "user?" (-> req ))
         (if (-> req :session :user/id)
           [:a.button.is-light {:href "/user/logout"} "Log out"]
           [:a.button.is-light {:href "/user/login"} "Log in"]))]]]]])

(defn- notification [req]
  (when req
    (when-let [flash (:flash req)]
      [:div {:class (str "notification " (str "is-" (name (:type flash))))}
       (:message flash)])))

(defn base-view
  [req content & {:keys [enable-abc? custom-script body-load-hook]}]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Hello Bulma!"]
    [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css"}]]
   [:body (when body-load-hook {:onload body-load-hook})
    [:section.section
     [:div.container
      (navbar req)
      (notification req)
      content]]
    (when custom-script
      [:script {:type "text/javascript"}
       custom-script])
    (when enable-abc?
      [:div
       [:script {:type "text/javascript"
                 :src  "https://cdn.jsdelivr.net/npm/abcjs@6.0.2/dist/abcjs-basic-min.js"}]
       [:script {:type "text/javascript"
                 :src  "js/abc-player.js"}]])
    ]])

(defn home-view [req user-id]
  (base-view req [:h2 (str "Hi! " user-id)]))
