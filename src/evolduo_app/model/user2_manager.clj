(ns evolduo-app.model.user2-manager
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [evolduo-app.model.evolution-manager :as em]))

(defn insert-user
  [db user]
  (sql/insert! db :user user))

(defn find-user-by-email
  [db email]
  (first (sql/query db ["select * from user where email = ?" email] {:builder-fn em/sqlite-builder}))) ;; TODO first? query by attrs

(defn verify-user
  [db token]
  (when-let [res (sql/update! db :user {:verified 1} {:verification_token token} {:builder-fn em/sqlite-builder})]
    res)) ;; TODO factor out builder

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :user {:id 1} {:builder-fn em/sqlite-builder})))
