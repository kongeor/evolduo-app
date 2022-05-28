(ns evolduo-app.model.mail
  (:require [next.jdbc.sql :as sql]
            [evolduo-app.model.evolution :as em]))

(defn insert
  [db mail]
  ;; TODO validate
  (sql/insert! db :mail mail))

(defn mark-as-sent [db id]
  (sql/update! db :mail {:sent true} {:id id}))

(defn find-unsent-mails [db]
  (sql/find-by-keys db :mail {:sent false} {:builder-fn em/sqlite-builder
                                            :limit 10}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from mail"] {:builder-fn em/sqlite-builder}))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/update! db :mail {:type "signup"} {:sent false} {:builder-fn em/sqlite-builder}))
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-unsent-mails db)))
