(ns evolduo-app.model.stats
  (:require [next.jdbc.sql :as sql]))

(def ratings-q
  "
  select date(g), coalesce (d.count, 0) as count
  from generate_series(
                        '2022-10-19 00:00'::timestamp,
                        'now()',
          '1 day') as g
  left join
  (select date(created_at) as date, count(*)  from ratings
    group by date
    order by date) as d on g.g = d.date
  where g.g > '2022-10-10'
  ")

(defn ratings-stats [db]
  (sql/query db [ratings-q]))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (ratings-stats db)))
