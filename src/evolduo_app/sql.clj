(ns evolduo-app.sql
  (:require [next.jdbc.sql :as sql]))

(defn insert! [db table key-map]
  (let [res (sql/insert! db table key-map)
        id ((keyword "last_insert_rowid()") res)]
    {:id id}))
