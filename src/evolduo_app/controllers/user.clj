(ns evolduo-app.controllers.user
  (:require [clojure.tools.logging :as log]
            [evolduo-app.middleware :as middleware]
            [evolduo-app.model.user :as user]
            [evolduo-app.request :as req]
            [evolduo-app.response :as r]
            [evolduo-app.schemas :as s]
            [evolduo-app.views.user :as user-views]
            [ring.util.response :as response]))

(defn signup-form
  [req]
  (r/render-html user-views/signup-form req))

(defn signup [req]
  (let [db (:db req)
        captcha-code (middleware/anti-forgery->captcha-code)
        real-ip (req/get-x-forwarded-for-header req)
        params (-> req :params (select-keys [:email :password :password_confirmation :captcha]))
        sanitized-data (s/decode-and-validate s/Signup params)]
    (cond
      (:error sanitized-data)
      (r/render-html user-views/signup-form req {:signup params
                                                 :errors (:error sanitized-data)})

      (not= captcha-code (-> sanitized-data :data :captcha))
      (do
        ;; TODO update for nginx
        ;; TODO reset for invalid submissions
        ;; Fix
        (log/warn "Invalid signup attempt from" real-ip)
        (r/render-html user-views/signup-form req {:signup params
                                                   :errors {:captcha ["not valid"]}}))

      (user/get-registered-user db (-> sanitized-data :data :email))
      (r/render-html user-views/signup-form req {:signup params
                                                 :errors {:email ["this email is already in use"]}})

      ;; TODO check vip list
      #_(not ((set (:vip_list settings)) (-> sanitized-data :data :email)))
      #_(r/render-html user-views/signup-form req {:signup params
                                                 :errors {:email ["unfortunately you are not in the vip list"]}})

      :else
      (do
        (if-let [user (user/upsert!
                        db
                        (-> sanitized-data :data :email)
                        (-> sanitized-data :data :password))]
          (->
            (r/redirect "/"
              :flash {:type :info :message "Welcome to Evolduo! You should receive an email verification in the next few minutes. Don't forget to check your spam folder"})
            (assoc-in [:session :user/id] (:id user)))
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
        real-ip (req/get-x-forwarded-for-header req)
        session (:session req)]
    (if-let [user (user/login-user db email password)]
      (let [session' (assoc session :user/id (:id user))]
        (-> (response/redirect "/")
          (assoc :session session')))
      (do
        (log/warn "Invalid login attempt from" real-ip)
        (r/render-html user-views/login-form req {:login        {:email email}
                                                  :notification {:type    "danger"
                                                                 :message "Invalid email or password"}})))))

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
    (if-let [res (user/verify-user db token)]
      (do
        (assoc (response/redirect "/")
          :flash {:type :info :message "Cool, you are now verified!"}))
      (assoc (response/redirect "/")
        :flash {:type :danger :message "Oops, something was wrong."}))))

(defn unsubscribe [req]
  (let [db (:db req)
        token (-> req :params :token)]
    ;; TODO rotate token
    (if-let [res (user/unsubscribe! db token)]
      (do
        (assoc (response/redirect "/")
          :flash {:type :info :message "Your subscription settings have been updated"}))
      (assoc (response/redirect "/")
        :flash {:type :danger :message "Oops, something was wrong."}))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (login-user db "foo@example.com" "12345")))

(defn account
  [req]
  (if-let [user-id (req/user-id req)]
    (let [db   (:db req)
          user (user/find-user-by-id db user-id)]
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
        (user/update-subscription db user-id (:data sanitized-data))
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
        (user/delete-user db user-id)
        (r/logout {:message "Your account has been deleted"})
        ))
    ))
