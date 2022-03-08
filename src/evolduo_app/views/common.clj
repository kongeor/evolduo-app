(ns evolduo-app.views.common)

(defn- navbar []
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
     [:a.navbar-item {:href "/evolution/list"} "Evolutions"]]
    [:div.navbar-end
     [:div.navbar-item
      [:div.buttons
       [:a.button.is-primary
        [:strong "Sign up"]]
       [:a.button.is-light {:href "/user/login"} "Log in"]]]]]])

(defn base-view [content]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Hello Bulma!"]
    [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css"}]]
   [:body
    [:section.section
     [:div.container
      (navbar)
      content]]]])

(defn home-view [user-id]
  (base-view [:h2 (str "Hi! " user-id)]))
