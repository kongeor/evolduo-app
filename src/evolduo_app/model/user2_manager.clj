(ns evolduo-app.model.user2-manager
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn insert-user
  [db user]
  (sql/insert! db :user user))

(defn find-user-by-email
  [db email]
  (first (sql/query db ["select * from user where email = ?" email]))) ;; TODO first? query by attrs

