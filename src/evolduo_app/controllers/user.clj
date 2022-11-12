(ns evolduo-app.controllers.user
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.user :as user]
            [evolduo-app.rand :as er]
            [evolduo-app.request :as req]
            [evolduo-app.response :as r]
            [evolduo-app.schemas :as s]
            [evolduo-app.views.user :as user-views]
            [ring.util.response :as response]))


(defn signup-form
  [{:keys [session] :as req}]
  (let [captcha (er/random-num 6)]
    (->
      (r/render-html user-views/signup-form req {:captcha captcha :signup {:newsletters "on"}})
      (assoc :session (assoc session :captcha captcha)))))

(defn signup [req]
  (let [db (:db req)
        captcha-code (or (-> req :session :captcha) "")
        real-ip (req/get-x-forwarded-for-header req)
        params (-> req :params (select-keys [:email :password :password_confirmation :captcha :newsletters]))
        sanitized-data (s/decode-and-validate s/Signup params)]
    (cond
      (:error sanitized-data)
      (r/render-html user-views/signup-form req {:captcha captcha-code
                                                 :signup params
                                                 :errors (:error sanitized-data)})

      (not= captcha-code (-> sanitized-data :data :captcha))
      (do
        ;; TODO update for nginx
        ;; TODO reset for invalid submissions
        ;; Fix
        (log/warn "Invalid signup attempt from" real-ip)
        (r/render-html user-views/signup-form req {:captcha captcha-code
                                                   :signup params
                                                   :errors {:captcha ["not valid"]}}))

      (user/get-registered-user db (-> sanitized-data :data :email))
      (r/render-html user-views/signup-form req {:captcha captcha-code
                                                 :signup params
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
                        (-> sanitized-data :data :password)
                        (-> sanitized-data :data :newsletters))]
          (->
            (r/redirect "/"
              :flash {:type :info :message [:span
                                            (str "Welcome to Evolduo! You should receive an email verification shortly. "
                                              "Don't forget to check your spam folder. Still not there? Check ")
                                            [:a {:target "_blank" :href "https://github.com/kongeor/evolduo-app/blob/main/doc/known_issues.md#oh-no-my-email"} "this"]
                                            "."]})
            (assoc-in [:session :user/id] (:id user)))
          (r/redirect "/"
            :flash {:type :danger :message "Ooops"}))))
    ))

(defn login-form
  [req]
  (r/render-html user-views/login-form req))

(defn login [{:keys [db session] :as req}]
  (let [email (-> req :params :email)
        password (-> req :params :password)
        sanitized-data (s/decode-and-validate s/Login {:email email :password password})
        real-ip (req/get-x-forwarded-for-header req)]
    (cond
      (:error sanitized-data)
      (do
        (log/warn "Invalid login attempt from" real-ip)
        (r/render-html user-views/login-form req {:login        {:email email}
                                                  :notification {:type    "danger"
                                                                 :message "Invalid email or password"}}))

      :else
      (if-let [user (user/login-user db
                                     (-> sanitized-data :data :email)
                                     (-> sanitized-data :data :password))]
        (let [session' (assoc session :user/id (:id user))]
          (-> (response/redirect "/")
              (assoc :session session')))
        ))))

(defn logout-user [req]
  (r/logout {:message "You have successfully been logged out"}))

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

(defn set-password-form [{:keys [db] :as req}]
  (let [real-ip (req/get-x-forwarded-for-header req)]
    (if (user/find-user-by-password-reset-token db (-> req :params :token))
      (r/render-html user-views/set-password-form req)
      (do
        (log/warn "Invalid reset password token provided from" real-ip)
        (r/redirect "/"
          :flash {:type :danger :message [:span "Password reset token is invalid or expired"]})))))

(defn set-password [{:keys [db] :as req}]
  (let [params         (-> req :params (select-keys [:token :password :password_confirmation]))
        sanitized-data (s/decode-and-validate s/PasswordSet params)
        {:keys [token password]} (-> sanitized-data :data)]
    (cond
      (:error sanitized-data)
      (r/render-html user-views/set-password-form req {:errors (:error sanitized-data)})

      :else
      (if-let [user (user/find-user-by-password-reset-token db token)]
        (do
          (user/update-user-password! db (:id user) password)
          (r/redirect "/"
                      :flash {:type :info :message [:span "Your password has been updated successfully"]}))
        (r/redirect "/"
                    :flash {:type :danger :message [:span "Password reset is token invalid or expired"]})))
      ))

(defn password-reset-save [{:keys [db] :as req}]
  (let [captcha-code   (or (-> req :session :captcha) "")
        real-ip        (req/get-x-forwarded-for-header req)
        params         (-> req :params (select-keys [:email :captcha]))
        sanitized-data (s/decode-and-validate s/PasswordReset params)]
    (cond
      (:error sanitized-data)
      (r/render-html user-views/password-reset-form req {:captcha captcha-code
                                                         :reset params
                                                         :errors (:error sanitized-data)})

      (not= captcha-code (-> sanitized-data :data :captcha))
      (do
        (log/warn "Invalid password request attempt from" real-ip)
        (r/render-html user-views/password-reset-form req {:captcha captcha-code
                                                           :reset params
                                                           :errors {:captcha ["not valid"]}}))

      ;; TODO check too many requests and update fail2ban

      :else
      (do
        (let [user (user/find-user-by-email
                     db
                     (-> sanitized-data :data :email))]
          (when user
            (log/info "Password reset request from user" (:id user))
            (user/update-password-reset-token db (:id user)))
          (r/redirect "/"
                      :flash {:type :info :message [:span
                                                    (str "A password reset email has been sent to "
                                                         (-> sanitized-data :data :email) ". "
                                                         "Don't forget to check your spam folder.")]})))
      ))
  )

(defn password-reset-form [{:keys [session] :as req}]
  (let [captcha (er/random-num 6)]
    (->
      (r/render-html user-views/password-reset-form req {:captcha captcha})
      (assoc :session (assoc session :captcha captcha)))))
