(ns evolduo-app.model.rating
  (:require [next.jdbc.sql :as sql]))

(defn insert-rating
  [db rating]
  (sql/insert! db :ratings rating))

(defn find-iteration-ratings-for-user [db iteration-id user-id]
  (sql/find-by-keys db :ratings {:iteration_id iteration-id
                                 :user_id user-id}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (group-by :chromosome_id (find-iteration-ratings-for-user db 1 1))))

;; TODO check fields order
;; TODO unique index on type/value/chromosome_id/user_id

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :ratings {:id 3})))
