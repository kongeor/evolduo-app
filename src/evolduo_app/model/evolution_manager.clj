(ns evolduo-app.model.evolution-manager
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs])
  (:import (java.sql ResultSet ResultSetMetaData)))

;; util

;; https://stackoverflow.com/questions/63017628/how-do-i-read-bool-columns-from-sqlite-into-bool-clojure-values-with-next-jdbc
(defn sqlite-column-by-index-fn [builder ^ResultSet rs ^Integer i]
  (let [rsm ^ResultSetMetaData (:rsmeta builder)]
    (rs/read-column-by-index
      (let [col-type (.getColumnTypeName rsm i)]
        (case col-type
          "BOOL" (.getBoolean rs i)
          "TIMESTAMP" (.getTimestamp rs i)
          (.getObject rs i)))
      rsm i)))

(def sqlite-builder (rs/builder-adapter rs/as-maps sqlite-column-by-index-fn))
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
