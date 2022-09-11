(ns evolduo-app.model.rating
  (:require [next.jdbc.sql :as sql]
            [honey.sql :as h]))

(defn insert-rating
  [db rating]
  (sql/insert! db :ratings rating))

(defn find-iteration-ratings-for-user [db evolution-id iteration-num user-id]
  (let [q-sqlmap {:select [[:i/id :iteration_id]
                           [:r/chromosome_id :chromosome_id]]
                  :from   [[:ratings :r]]
                  :join   [[:iterations :i] [:= :r/iteration_id :i/id]]
                  :where  [:and
                           [:= :r/user_id user-id]
                           [:= :i/evolution_id evolution-id]
                           [:= :i/num iteration-num]]}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-iteration-ratings-for-user db 1 1 1)
    #_(group-by :chromosome_id (find-iteration-ratings-for-user db 1 5 1))))

;; TODO check fields order
;; TODO unique index on type/value/chromosome_id/user_id

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :ratings {:id 3})))

(defn find-iteration-ratings
  "Returns a map of chromosome_id -> rating (aggregate sum)"
  [db evolution-id iteration-num]
  (let [q-sqlmap {:select   [[[:sum :r/value] :rating]
                             [:r/chromosome_id :chromosome_id]]
                  :from     [[:ratings :r]]
                  :join     [[:iterations :i] [:= :r/iteration_id :i/id]
                             [:chromosomes :c] [:= :r/chromosome_id :c/id]]
                  :where    [:and
                             [:= :i/evolution_id evolution-id]
                             [:= :i/num iteration-num]]
                  :group-by [:r/chromosome_id]}]
    (let [results (sql/query db (h/format q-sqlmap))]
      (reduce #(assoc % (:chromosome_id %2) (:rating %2)) {} results))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-iteration-ratings db 67 2)))
