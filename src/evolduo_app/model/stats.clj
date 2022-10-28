(ns evolduo-app.model.stats
  (:require [clojure.string :as str]
            [next.jdbc.sql :as sql]))

(defn stats-q [type]
  (str/replace
    "
  select date(g), coalesce (d.count, 0) as count
  from generate_series(
                        '2022-10-19 00:00'::timestamp,
                        'now()',
          '1 day') as g
  left join
  (select date(created_at) as date, count(*)  from :type
    group by date
    order by date) as d on g.g = d.date
  where g.g > '2022-10-18'
  " ":type" type))

(defn- inst->local-date [i]
  (str (.toLocalDate i)))

(defn- stats-for-type [db type]
  (let [data (sql/query db [(stats-q type)])]
    {:dates  (mapv (comp inst->local-date :date) data)
     :counts (mapv :count data)}))

(defn stats [db]
  {:ratings (stats-for-type db "ratings")
   :users   (stats-for-type db "users")
   :chromosomes (stats-for-type db "chromosomes")
   :evolutions (stats-for-type db "evolutions")
   })

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (stats db)))
