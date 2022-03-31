(ns evolduo-app.model.evolution-manager
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [clojure.string :as string]
            [evolduo-app.music :as music])
  (:import (java.sql ResultSet ResultSetMetaData)))

;; util

(defn blob->int-vec [str-line]
  (if (string/blank? str-line)
    []
    (let [s' (.substring str-line 1 (count str-line))
          l (count s')
          s'' (.substring s' 0 (dec l))]
      (->> (string/split s'' #" ")
        (map #(Integer/parseInt %))))))

(defn sql-insert! [db table key-map]
  (let [res (sql/insert! db table key-map)
        id ((keyword "last_insert_rowid()") res)]
    {:id id}))

(comment
  (blob->int-vec "[1 2 3]"))

;; https://stackoverflow.com/questions/63017628/how-do-i-read-bool-columns-from-sqlite-into-bool-clojure-values-with-next-jdbc
(defn sqlite-column-by-index-fn [builder ^ResultSet rs ^Integer i]
  (let [rsm ^ResultSetMetaData (:rsmeta builder)]
    (rs/read-column-by-index
      (let [col-type (.getColumnTypeName rsm i)]
        (case col-type
          "BOOL" (.getBoolean rs i)
          "TIMESTAMP" (.getTimestamp rs i)
          "BLOB" (blob->int-vec (.getObject rs i))
          (.getObject rs i)))
      rsm i)))

(def sqlite-builder (rs/builder-adapter rs/as-unqualified-maps sqlite-column-by-index-fn))
;;

(defn get-evolution-by-id
  "Given an evolution ID, return the evolution record."
  [db id]
  (sql/get-by-id db :evolution id {:builder-fn sqlite-builder}))

(defn get-evolutions
  [db]
  (sql/query db
    ["
select e.*
 from evolution e
"] {:builder-fn sqlite-builder}))

(comment
  (get-evolutions (:database.sql/connection integrant.repl.state/system)))

(def sample-chromo {:created_at (java.util.Date.)
                    :fitness 42
                    :genes [1 2 3 4 5 -2 -2]
                    :abc "C C C"
                    :iteration_id -1})

(defn save-evolution
  [db evolution]
  (jdbc/with-transaction [tx db]
    (let [evol-insert (sql-insert! tx :evolution evolution)
          iter-insert (sql-insert! tx :iteration {:created_at (java.util.Date.)
                                                  :num 0
                                                  :evolution_id (:id evol-insert)})]
      (doall
        (map #(sql-insert! tx :chromosome
                (let [{:keys [key mode pattern]} evolution
                      ;; TODO use strings instead
                      genes (music/random-track {:key key :measures 4 :mode (keyword mode)})
                      abc (music/->abc-track {:key key :mode (keyword mode) :pattern pattern}
                            {:genes genes})
                      ]
                  (assoc % :iteration_id (:id iter-insert)
                           :genes genes
                           :abc abc))) (repeat 2 sample-chromo))))))

(defn find-last-iteration-id-for-evolution [db evolution-id]
  ;; TODO fix
  (first (vals (first (sql/query db ["select max(id) from iteration where evolution_id = ?" evolution-id])))))

(defn find-iteration-chromosomes [db iteration-id]
  (sql/find-by-keys db :chromosome {:iteration_id iteration-id} {:builder-fn sqlite-builder}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-last-iteration-for-evolution db 1)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-iteration-chromosomes db 1)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/insert! db :iteration {:created_at (java.util.Date.)
                                :num 0
                                :evolution_id 1})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/insert! db :iteration {:created_at (java.util.Date.)
                                :num 0
                                :evolution_id 1})))
(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/insert! db :chromosome {:created_at (java.util.Date.)
                                 :fitness 42
                                 :genes [1 2 3 4 5 -2 -2]
                                 :abc "C C C"
                                 :iteration_id 1})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :chromosome {:id 1} {:builder-fn sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from chromosome"] {:builder-fn sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from iteration"] {:builder-fn sqlite-builder})))
