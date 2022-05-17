(ns evolduo-app.model.user2-manager
  (:require [next.jdbc.sql :as sql]
            [evolduo-app.sql :as esql]
            [evolduo-app.model.evolution-manager :as em]
            [crypto.password.pbkdf2 :as password]
            [crypto.random :as rnd])
  (:import (java.time Instant)))

(defn- insert-user
  [db user]
  (esql/insert! db :user user))

;; TODO pepper
(defn create
  "Returns only the user id"
  [db email pass]
  (let [salt (rnd/hex 32)
        encrypted (password/encrypt (str salt pass))
        verification_token (rnd/hex 100)]
    ;; TODO schema check - adjust defaults
    (insert-user db {:created_at         (Instant/now)
                     :role               "admin"
                     :email              email
                     :salt               salt
                     :password           encrypted
                     :verified           true
                     :verification_token verification_token
                     :subscription       {:notifications true
                                          :announcements true}})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (create db "foo@example.com" "12345"))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from user"])))

(defn find-user-by-email
  [db email]
  (first (sql/query db ["select * from user where email = ?" email] {:builder-fn em/sqlite-builder}))) ;; TODO first? query by attrs

(defn find-user-by-id
  [db id]
  (-> (sql/get-by-id db :user id {:builder-fn em/sqlite-builder})
    (select-keys [:id
                  :email
                  :verification_token
                  :subscription])))

(defn verify-user
  [db token]
  (when-let [res (sql/update! db :user {:verified 1
                                        :verification_token nil
                                        :verified_at (Instant/now)} {:verification_token token} {:builder-fn em/sqlite-builder})]
    res)) ;; TODO factor out builder

(defn login-user [db email pass]
  (if-let [user (find-user-by-email db email)]
    (let [salt (:salt user)
          encrypted (:password user)]
      (when (password/check (str salt pass) encrypted)
        (select-keys user [:id :email])))))


(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :user {:id 1} {:builder-fn em/sqlite-builder})))

;; TODO updated at?
(defn update-subscription
  [db user-id subscription]
  (when-let [res (sql/update! db :user {:subscription subscription} {:id user-id} {:builder-fn em/sqlite-builder})]
    res)) ;; TODO factor out builder

(defn delete-user
  [db user-id]
  (when-let [res (sql/update! db :user {:deleted true
                                        :email  (str (.toEpochMilli (Instant/now)) "-deleted@example.com")
                                        :verification_token nil
                                        :password nil
                                        :salt nil
                                        :subscription "{}"}
                   {:id user-id} {:builder-fn em/sqlite-builder})]
    res)) ;; TODO factor out builder
