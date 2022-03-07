(ns evolduo-app.controllers.user2
  (:require [ring.util.response :as resp]
            [ring.middleware.anti-forgery :as anti-forgery]
            [ring.util.anti-forgery :as af-util]
            [hiccup.core :as hiccup]
            [crypto.random :as rnd]
            [crypto.password.pbkdf2 :as password]
            [evolduo-app.model.user2-manager :as user2]
            [ring.util.response :as response]))

(defn- base-view [content]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Hello Bulma!"]
    [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css"}]]
   [:body
    [:section.section
     [:div.container
      content]]]])

#_(af-util/anti-forgery-field)

(defn login-view []
  (base-view
    [:form {:action "/user/login" :method "post"}
     [:input {:type "hidden" :id"__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
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

;; TODO move

(defn- home-view [user-id]
  (base-view [:h2 (str "Hi! " user-id)]))

(defn home
  [req]
  (let [session (:session req)
        user-id (:user/id session)]
    (-> (resp/response (hiccup/html (home-view user-id)))
      (resp/content-type "text/html"))))

(defn login
  "Display the add/edit form."
  [req]
  (let [db (:db req)]
    (-> (resp/response (hiccup/html (login-view)))
      (resp/content-type "text/html"))))

(defn create-user [db email pass]
  (let [salt (rnd/hex 32)
        encrypted (password/encrypt (str salt pass))]
     (user2/insert-user db {:email email
                            :salt salt
                            :password encrypted})))

;; TODO move to model
(defn login-user [db email pass]
  (if-let [user (user2/find-user-by-email db email)]
    (let [salt (:user/salt user)
          encrypted (:user/password user)]
      (when (password/check (str salt pass) encrypted)
        (println "! success!")
        (select-keys user [:user/id :user/email])))))

(defn login-user-handler [req]
  (let [db (:db req)
        email (-> req :params :email)
        password (-> req :params :password)
        session (:session req)]
    (println "ZZZZZZ" req)
    (if-let [user (login-user db email password)]
      (let [session' (assoc session :user/id (:user/id user))]
        (println "session'" session')
        (-> (response/redirect "/")
          (assoc :session session'))))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (login-user db "foo@example.com" "12345")))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (create-user db "foo@example.com" "12345")))

