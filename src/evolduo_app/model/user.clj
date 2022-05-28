(ns evolduo-app.model.user
  (:require [next.jdbc.sql :as sql]
            [evolduo-app.sql :as esql]
            [evolduo-app.model.evolution :as em]
            [evolduo-app.model.mail :as mail-model]
            [crypto.password.pbkdf2 :as password]
            [crypto.random :as rnd]
            [clojure.pprint :as pp]
            [next.jdbc :as jdbc])
  (:import (java.time Instant)))

(defn- insert-user
  [db user]
  (esql/insert! db :user user))

;; Not using pepper per https://stackoverflow.com/questions/16891729/best-practices-salting-peppering-passwords
(defn create
  "Returns only the user id"
  [db email pass]
  (let [encrypted (password/encrypt pass)
        verification_token (rnd/hex 100)]
    ;; TODO schema check - adjust defaults
    (insert-user db {:created_at         (Instant/now)
                     :role               "admin"
                     :email              email
                     :password           encrypted
                     :verified           true               ;; TODO fix
                     :verification_token verification_token
                     :subscription       {:notifications true
                                          :announcements true}})))

(defn create-stub
  "Create a stub user, someone that was invited for example but doesn't exist on the platform"
  [db email]
  ;; TODO schema check
  (insert-user db {:created_at         (Instant/now)
                   :role               "user"
                   :email              email
                   :subscription       {:notifications true
                                        :announcements false}}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (create db "foo@example.com" "12345"))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/update! db :user {:verified 1} {:email "foo@example.com"} ))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select json_extract(u.subscription, '$.notifications') as n from user u "]))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (pp/print-table (sql/query db ["select email, verified, deleted from user"]))))

(defn find-user-by-email
  [db email]
  (first (sql/query db ["select * from user where email = ?" email] {:builder-fn em/sqlite-builder}))) ;; TODO first? query by attrs

(defn get-registered-user
  [db email]
  (first (sql/query db ["select * from user where email = ? and password is not null" email] {:builder-fn em/sqlite-builder}))) ;; TODO first? query by attrs

(defn upsert!
  "Doesn't return something useful (although it could)"
  [db email pass]
  (jdbc/with-transaction [tx db]
    (let [{:keys [id]}
          (if-let [user (find-user-by-email tx email)]
            (let [encrypted          (password/encrypt pass)
                  verification_token (rnd/hex 100)]
              (sql/update!
                tx
                :user
                {:role               "admin"                ;; TODO updated?
                 :password           encrypted
                 :verified           true                   ;; TODO fix
                 :verification_token verification_token     ;; TODO duplicated
                 :subscription       {:notifications true   ;; TODO hm
                                      :announcements true}}
                {:email email})
              user)
            (create tx email pass))]
      (mail-model/insert tx {:created_at   (Instant/now)
                             :recipient_id id
                             :type         "signup"
                             :data         {}}))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (get-registered-user db "bar@example.com")))

(defn find-user-by-id
  [db id]
  (-> (sql/get-by-id db :user id {:builder-fn em/sqlite-builder})
    (select-keys [:id
                  :email
                  :verification_token
                  :subscription])))

(defn create-stub-or-get!
  "In the case of stub will just provide id"
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
  (when-let [res (sql/update! db :user {:verified 1
                                        :verification_token nil
                                        :verified_at (Instant/now)} {:verification_token token} {:builder-fn em/sqlite-builder})]
    res)) ;; TODO factor out builder

(defn login-user [db email pass]
  (if-let [user (find-user-by-email db email)]
    (if-let [encrypted (:password user)]
      (when (password/check pass encrypted)
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
