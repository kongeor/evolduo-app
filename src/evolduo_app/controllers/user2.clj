(ns evolduo-app.controllers.user2
  (:require [ring.util.response :as resp]
            [hiccup.core :as hiccup]
            [crypto.random :as rnd]
            [crypto.password.pbkdf2 :as password]
            [evolduo-app.model.user2-manager :as user2]
            [evolduo-app.views.user :as user-views]
            [evolduo-app.views.common :as common-views]
            [ring.util.response :as response]))

;; TODO move

(defn home
  [req]
  (let [session (:session req)
        user-id (:user/id session)]
    (-> (resp/response (hiccup/html (common-views/home-view user-id)))
      (resp/content-type "text/html"))))

(defn login
  "Display the add/edit form."
  [req]
  (let [db (:db req)]
    (-> (resp/response (hiccup/html (user-views/login-view)))
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

