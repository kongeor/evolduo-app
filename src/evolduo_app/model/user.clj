(ns evolduo-app.model.user
  (:require [next.jdbc.sql :as sql]
            [evolduo-app.model.evolution :as em]
            [evolduo-app.model.mail :as mail-model]
            [crypto.password.pbkdf2 :as password]
            [crypto.random :as rnd]
            [clojure.pprint :as pp]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.time Instant)))

(defn- insert-user
  [db user]
  (sql/insert! db :users user))

;; Not using pepper per https://stackoverflow.com/questions/16891729/best-practices-salting-peppering-passwords
(defn create
  "Returns only the user id"
  [db email pass newsletters]
  (let [encrypted (password/encrypt pass)
        verification_token (rnd/hex 100)
        unsubscribe_token (rnd/hex 100)]
    ;; TODO schema check - adjust defaults
    (insert-user db {:role               "user"
                     :email              email
                     :password           encrypted
                     :verified           false
                     :verification_token verification_token
                     :subscription       {:notifications true
                                          :announcements (some? newsletters)}
                     :unsubscribe_token  unsubscribe_token})))

(defn create-stub
  "Create a stub user, someone that was invited for example but doesn't exist on the platform"
  [db email]
  ;; TODO schema check
  (let [unsubscribe_token (rnd/hex 100)]
    (insert-user db {:role              "user"
                     :email             email
                     :subscription      {:notifications true
                                         :announcements false}
                     :unsubscribe_token unsubscribe_token})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (create db "foo@example.com" "12345"))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/update! db :users {:verified 1} {:email "foo@example.com"} ))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select json_extract(u.subscription, '$.notifications') as n from user u "]))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (pp/print-table (sql/query db ["select email, verified, deleted from users"]))))

(defn find-user-by-email
  [db email]
  (first (sql/find-by-keys db :users {:email email})))

(defn get-registered-user
  [db email]
  (first (sql/query db ["select * from users where email = ? and password is not null" email]))) ;; TODO first? query by attrs

(def user-keys [:id
                :email
                :verified
                :verification_token
                :subscription
                :unsubscribe_token
                :password_reset_token])

(defn upsert!
  [db email pass newsletters]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [user (if-let [user (find-user-by-email tx-opts email)]
                   (let [encrypted          (password/encrypt pass)
                         verification_token (rnd/hex 100)
                         unsubscribe_token (rnd/hex 100)]
                     (sql/update!
                       tx-opts
                       :users
                       {:role               "user"               ;; TODO updated?
                        :password           encrypted
                        :verified           false
                        :verification_token verification_token   ;; TODO duplicated
                        :subscription       {:notifications true ;; TODO hm
                                             :announcements (some? newsletters)}
                        :unsubscribe_token unsubscribe_token}
                       {:email email})
                     user)
                   (create tx-opts email pass newsletters))]
        (mail-model/insert tx-opts {:recipient_id (:id user)
                                    :type         "signup"
                                    :data         {}})
        (select-keys user user-keys)))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (get-registered-user db "bar@example.com")))

;; TODO verify usages (create verified?(user-id) function), remove unused calls
(defn find-user-by-id
  [db id]
  (-> (sql/get-by-id db :users id)
    (select-keys user-keys)))

(defn create-stub-or-get!
  [db email]
  (or
    (find-user-by-email db email)
    (create-stub db email)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (create-stub-or-get! db "bar@example.com"))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (upsert! db "bar@example.com" "12345")))

(defn verify-user
  [db token]
  (when-let [res (sql/update! db :users {:verified true
                                         :verification_token nil
                                         :verified_at (Instant/now)} {:verification_token token})]
    res))

(defn unsubscribe!
  [db token]
  (sql/update! db :users {:subscription {}
                          :verified_at  (Instant/now)} {:unsubscribe_token token}))

(defn login-user [db email pass]
  (if-let [user (find-user-by-email db email)]
    (if-let [encrypted (:password user)]
      (when (password/check pass encrypted)
        (select-keys user [:id :email])))))


(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :users {:id 1})))

;; TODO updated at?
(defn update-subscription
  [db user-id subscription]
  (when-let [res (sql/update! db :users {:subscription subscription} {:id user-id})]
    res))

(defn delete-user
  [db user-id]
  (when-let [res (sql/update! db :users {:deleted true
                                         :email  (str (.toEpochMilli (Instant/now)) "-deleted@example.com")
                                         :verification_token nil
                                         :unsubscribe_token nil
                                         :password nil
                                         :subscription {}}
                   {:id user-id})]
    res))

(defn update-password-reset-token
  "Updates the token and sends an email to the user"
  [db user-id]
  (when user-id
    (let [token (rnd/hex 100)]
      (jdbc/with-transaction [tx db]
        (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
          (sql/update! tx-opts :users {:password_reset_token   token
                                       :password_reset_sent_at (Instant/now)}
                       {:id user-id})
          (mail-model/insert tx-opts {:recipient_id user-id
                                      :type         "password-reset"
                                      :data         {}}))))))

(defn find-user-by-password-reset-token [db token]
  (first (sql/find-by-keys db :users {:password_reset_token token})))

(defn update-user-password! [db user-id password]
  (let [encrypted (password/encrypt password)]
    (sql/update! db :users {:password encrypted
                            :password_reset_token nil
                            :password_reset_sent_at nil} {:id user-id})))

(defn find-users-with-enabled-notifications [db]
  (map #(select-keys % user-keys)
       (sql/query db ["select * from users where (cast (subscription ->> 'notifications' as boolean) is true)"])))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-users-with-enabled-notifications db)))
