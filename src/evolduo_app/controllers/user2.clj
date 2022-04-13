(ns evolduo-app.controllers.user2
  (:require [ring.util.response :as resp]
            [hiccup.core :as hiccup]
            [crypto.random :as rnd]
            [crypto.password.pbkdf2 :as password]
            [evolduo-app.model.user2-manager :as user2]
            [evolduo-app.views.user :as user-views]
            [evolduo-app.views.common :as common-views]
            [ring.util.response :as response])
  (:import (java.util Date)))

;; TODO move

(defn home
  [req]
  (let [session (:session req)
        user-id (:user/id session)]
    (-> (resp/response (hiccup/html (common-views/home-view req user-id)))
      (resp/content-type "text/html"))))

(defn login
  "Display the add/edit form."
  [req]
  (let [db (:db req)]
    (-> (resp/response (hiccup/html (user-views/login-view req)))
      (resp/content-type "text/html"))))

(defn create-user [db email pass]
  (let [salt (rnd/hex 32)
        encrypted (password/encrypt (str salt pass))
        verification_token (rnd/hex 100)]
     (user2/insert-user db {:created_at (Date.)
                            :email      email
                            :salt       salt
                            :password   encrypted
                            :verification_token verification_token})))

;; TODO move to model
(defn login-user [db email pass]
  (if-let [user (user2/find-user-by-email db email)]
    (let [salt (:salt user)
          encrypted (:password user)]
      (when (password/check (str salt pass) encrypted)
        (select-keys user [:user/id :user/email])))))

(defn login-user-handler [req]
  (let [db (:db req)
        email (-> req :params :email)
        password (-> req :params :password)
        session (:session req)]
    (if-let [user (login-user db email password)]
      (let [session' (assoc session :user/id (:user/id user))]
        (-> (response/redirect "/")
          (assoc :session session'))))))

(defn verify-user [req]
  (let [db (:db req)
        token (-> req :params :token)]
    ;; TODO
    ;; sentry event?
    ;; delete verification token
    ;; login after verification
    ;; ensure users are not verified again
    ;; add verification date?
    (if-let [res (user2/verify-user db token)]
      (do
        (assoc (response/redirect "/")
          :flash {:type :info :message "Cool, you are now verified!"}))
      (assoc (response/redirect "/")
        :flash {:type :danger :message "Oops, something was wrong."}))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (login-user db "foo@example.com" "12345")))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (create-user db "foo@example.com" "12345")))

