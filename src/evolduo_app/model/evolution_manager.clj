(ns evolduo-app.model.evolution-manager
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [clojure.string :as string])
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

(comment
  (blob->int-vec "[1 2 3]"))

;; https://stackoverflow.com/questions/63017628/how-do-i-read-bool-columns-from-sqlite-into-bool-clojure-values-with-next-jdbc
(defn sqlite-column-by-index-fn [builder ^ResultSet rs ^Integer i]
  (let [rsm ^ResultSetMetaData (:rsmeta builder)]
    (rs/read-column-by-index
      (let [col-type (.getColumnTypeName rsm i)]
        (println "***" col-type)
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
  (sql/get-by-id db :evolution id))

(defn get-evolutions
  [db]
  (sql/query db
    ["
select e.*
 from evolution e
"] {:builder-fn sqlite-builder}))

(comment
  (get-evolutions (:database.sql/connection integrant.repl.state/system)))

(defn save-evolution
  [db evolution]
  (let [id (:id evolution)]
    (println "**" db evolution)
    (sql/insert! db :evolution evolution)
    #_(if (and id (not (zero? id)))
      (sql/update! db :addressbook
        (dissoc user :addressbook/id)
        {:id id})
      (sql/insert! db :addressbook
        (dissoc user :addressbook/id)))))


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
    (sql/query db ["select id, created_at from chromosome"] {:builder-fn sqlite-builder})))
