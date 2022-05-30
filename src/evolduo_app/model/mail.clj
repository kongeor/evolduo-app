(ns evolduo-app.model.mail
  (:require [next.jdbc.sql :as sql]
            [evolduo-app.model.evolution :as em]))

(defn insert
  [db mail]
  ;; TODO validate
  (sql/insert! db :mails mail))

(defn mark-as-sent [db id]
  (sql/update! db :mails {:sent true} {:id id}))

(defn find-unsent-mails [db]
  (sql/find-by-keys db :mails {:sent false} {:limit 10}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from mail"]))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/update! db :mail {:type "signup"} {:sent false}))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-unsent-mails db)))
