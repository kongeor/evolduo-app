(ns evolduo-app.model.reaction
  (:require [next.jdbc.sql :as sql]
            [evolduo-app.model.evolution-manager :as em]))

(defn insert-reaction
  [db reaction]
  (sql/insert! db :reaction reaction))

(defn find-iteration-reactions-for-user [db iteration-id user-id]
  (sql/find-by-keys db :reaction {:iteration_id iteration-id
                                    :user_id user-id} {:builder-fn em/sqlite-builder}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (group-by :chromosome_id (find-iteration-reactions-for-user db 1 1))))

;; TODO check fields order
;; TODO unique index on type/value/chromosome_id/user_id

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :reaction {:id 3} {:builder-fn em/sqlite-builder})))
