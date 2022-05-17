(ns evolduo-app.controllers.user2
  (:require [evolduo-app.response :as r]
            [evolduo-app.request :as req]
            [evolduo-app.model.user2-manager :as user2]
            [evolduo-app.views.user :as user-views]
            [evolduo-app.mail :as mail]
            [ring.util.response :as response]
            [evolduo-app.schemas :as s]
            [evolduo-app.music :as music]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(defn signup-form
  [req]
  (r/render-html user-views/signup-form req))

(defn signup [req]
  (let [db (:db req)
        action-seed (-> req :session :action-seed)
        settings (:settings req)
        params (-> req :params (select-keys [:email :password :password_confirmation :captcha]))
        sanitized-data (s/decode-and-validate s/Signup params)]
    (cond
      (:error sanitized-data)
      (r/render-html user-views/signup-form req {:signup params
                                                 :errors (:error sanitized-data)})

      (not=
        (music/get-chord-for-action-seed action-seed)
        (str/replace (-> sanitized-data :data :captcha) #"♯" "#")) ;; poundseption - for those about to copy/paste
      (do
        ;; TODO update for nginx
        ;; TODO reset for invalid submissions
        ;; Fix
        (log/warn "Invalid action seed from" (:remote-addr req))
        (r/render-html user-views/signup-form req {:signup params
                                                   :errors {:captcha ["not valid"]}}))

      (user2/find-user-by-email db (-> sanitized-data :data :email))
      (r/render-html user-views/signup-form req {:signup params
                                                 :errors {:email ["this email is already in use"]}})

      ;; TODO check vip list

      :else
      (do
        (if-let [user (user2/create
                        db
                        (-> sanitized-data :data :email)
                        (-> sanitized-data :data :password))]
          (let [user' (user2/find-user-by-id db (:id user))]
            (mail/send-welcome-email settings user')
            (->
              (r/redirect "/"
                :flash {:type :info :message "Great success!"})
              (assoc-in [:session :user/id] (:id user'))))
          (r/redirect "/"
            :flash {:type :danger :message "Ooops"}))))
    ))

(defn login-form
  [req]
  (r/render-html user-views/login-form req))

(defn login [req]
  (let [db (:db req)
        email (-> req :params :email)
        password (-> req :params :password)
        session (:session req)]
    (if-let [user (user2/login-user db email password)]
      (let [session' (assoc session :user/id (:id user))]
        (-> (response/redirect "/")
          (assoc :session session'))))))

(defn logout-user [req]
  (r/logout))

(defn verify-user [req]
  (let [db (:db req)
        token (-> req :params :token)]
    ;; TODO
    ;; sentry event?
    ;; delete verification token
    ;; login after verification
    ;; ensure users are not verified again
    ;; add verification date?
    ;; TODO always verified?
    (if-let [res (user2/verify-user db token)]
      (do
        (assoc (response/redirect "/")
          :flash {:type :info :message "Cool, you are now verified!"}))
      (assoc (response/redirect "/")
        :flash {:type :danger :message "Oops, something was wrong."}))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (login-user db "foo@example.com" "12345")))

(defn account
  [req]
  (if-let [user-id (req/user-id req)]
    (let [db   (:db req)
          user (user2/find-user-by-id db user-id)]
      (r/render-html user-views/account req {:user user}))
    (r/render-404)))

(defn update-subscription [req]
  (let [db (:db req)
        user-id (req/user-id req)
        data (-> req :params (select-keys [:announcements :notifications]))
        sanitized-data (s/decode-and-validate s/Subscription data)]
    ;; (println "params" (-> params first val type))

    (if (:error sanitized-data)
      (r/redirect "/user/account"
        :flash {:type :danger :message "Oops, something was wrong."})
      (do
        (user2/update-subscription db user-id (:data sanitized-data))
        (r/redirect "/user/account"
          :flash {:type :info :message "Your settings have been updated"})
        ))
    ))

(defn delete [req]
  (let [db (:db req)
        user-id (req/user-id req)
        verification (-> req :params :confirmation)
        ]
    (if (not= verification "I am awesome!")
      (r/redirect "/user/account"
        :flash {:type :danger :message "You are not awesome enough to delete your account. Please try again"})
      (do
        (user2/delete-user db user-id)
        (r/logout {:message "Your account has been deleted"})
        ))
    ))
