(ns evolduo-app.model.evolution-manager
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as h]
            [clojure.string :as string]
            [evolduo-app.music :as music]
            [clojure.string :as str]
            [next.jdbc.date-time]
            [clojure.tools.logging :as log])                          ;; enable java 8 times etc.
  (:import (java.sql ResultSet ResultSetMetaData)
           (java.util.concurrent TimeUnit)
           (java.time Instant)))

;; util

(defn blob->int-vec [str-line]
  (if (string/blank? str-line)
    []
    (let [s' (.substring str-line 1 (count str-line))
          l (count s')
          s'' (.substring s' 0 (dec l))]
      (->> (string/split s'' #" ")
        (mapv #(Integer/parseInt %))))))

(defn sql-insert! [db table key-map]
  (let [res (sql/insert! db table key-map)
        id ((keyword "last_insert_rowid()") res)]
    {:id id}))

(comment
  (blob->int-vec "[1 2 3]"))

;; https://stackoverflow.com/questions/63017628/how-do-i-read-bool-columns-from-sqlite-into-bool-clojure-values-with-next-jdbc
(defn sqlite-column-by-index-fn [builder ^ResultSet rs ^Integer i]
  (try
    (let [rsm ^ResultSetMetaData (:rsmeta builder)]
      (rs/read-column-by-index
        (let [col-type (.getColumnTypeName rsm i)]
          (case col-type
            "BOOL" (.getBoolean rs i)
            "TIMESTAMP" (.toInstant (.getTimestamp rs i))
            "BLOB" (blob->int-vec (.getObject rs i))
            (.getObject rs i)))
        rsm i))
    (catch Exception e
      (log/error e))))

(def sqlite-builder (rs/builder-adapter rs/as-unqualified-maps sqlite-column-by-index-fn))
;;

(defn find-evolution-by-id
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

(def sample-chromo {:created_at (Instant/now)
                    :fitness 42
                    :genes [1 2 3 4 5 -2 -2]
                    :abc "C C C"
                    :iteration_id -1})

(defn calc-evolve-after [^Instant now s]
  (let [tokens (str/split s #"-")
        amount (parse-long (first tokens))
        unit (condp = (second tokens)
               "min" TimeUnit/MINUTES
               "hour" TimeUnit/HOURS
               "day" TimeUnit/DAYS)]
    (.plusSeconds now
      (.toSeconds unit amount))))

(comment
  (calc-evolve-after (Instant/now) "8-hour"))

(defn save-evolution
  [db {:keys [version]} evolution]
  (jdbc/with-transaction [tx db]
    (let [evol-insert (sql-insert! tx :evolution evolution)
          now (Instant/now)
          evolve-after (calc-evolve-after now (:evolve_after evolution))
          iter-insert (sql-insert! tx :iteration {:created_at now
                                                  :evolve_after evolve-after
                                                  :num 0
                                                  :last true
                                                  :version version
                                                  :evolution_id (:id evol-insert)})]
      (doall
        (map #(sql-insert! tx :chromosome
                (let [{:keys [key mode pattern chord tempo]} evolution
                      ;; TODO use strings instead
                      genes (music/random-track {:key key :measures 4 :mode (keyword mode)})
                      abc (music/->abc-track {:key key :mode (keyword mode) :pattern pattern
                                              :chord chord :tempo tempo}
                            {:genes genes})
                      ]
                  (assoc % :iteration_id (:id iter-insert)
                           :genes (vec genes)               ;; TODO fix
                           :abc abc))) (repeat 2 sample-chromo))))))

(defn find-last-iteration-id-for-evolution [db evolution-id]
  ;; TODO fix
  (first (vals (first (sql/query db ["select max(id) from iteration where evolution_id = ?" evolution-id])))))

(defn find-iteration-chromosomes [db iteration-id]
  (sql/find-by-keys db :chromosome {:iteration_id iteration-id} {:builder-fn sqlite-builder}))

(defn find-chromosome-by-id [db chromosome-id]
  (sql/get-by-id db :chromosome chromosome-id {:builder-fn sqlite-builder}))

(defn find-iteration-by-id [db iteration-id]
  (sql/get-by-id db :iteration iteration-id {:builder-fn sqlite-builder}))

(defn increase-iteration-ratings [db iteration]
  (sql/update! db :iteration (update iteration :ratings inc) {:id (:id iteration)}))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-chromosome-by-id db 1)))

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
    (sql/query db ["select * from reaction"] {:builder-fn sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from iteration"] {:builder-fn sqlite-builder})))

(defn find-last-iteration-chromosomes [db evolution-id]
  (let [q-sqlmap {:select    [[:e/id :evolution_id]
                              [:i/id :iteration_id]
                              [:c.id :chromosome_id]
                              [:c.abc :abc]]
                  :from      [[:evolution :e]]
                  :join      [[:iteration :i] [:= :i/evolution_id :e/id]
                              [:chromosome :c] [:= :c/iteration_id :i/id]]
                  :where     [:= :e/id evolution-id]}]
    (sql/query db (h/format q-sqlmap) {:builder-fn sqlite-builder})))

(defn find-iteration-chromosomes [db evolution-id iteration-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:c.id :chromosome_id]
                           [:c.abc :abc]]
                  :from   [[:evolution :e]]
                  :join   [[:iteration :i] [:= :i/evolution_id :e/id]
                           [:chromosome :c] [:= :c/iteration_id :i/id]]
                  :where  [:and
                           [:= :e/id evolution-id]
                           [:= :i/id iteration-id]]}]
    (sql/query db (h/format q-sqlmap) {:builder-fn sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db (h/format q-sqlmap) {:builder-fn sqlite-builder})))


(defn find-user-active-evolutions [db user-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.key]
                           [:e.pattern]
                           [:i.num]
                           [:e.created_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]]
                  :from   [[:evolution :e]]
                  :join   [[:iteration :i] [:= :i/evolution_id :e/id]]
                  :where  [:and
                           [:= :e/user-id user-id]
                           [:<= :i.num :e.total_iterations]
                           [:= :i.last 1]]}]
    (sql/query db (h/format q-sqlmap) {:builder-fn sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-user-active-evolutions db 1)))

(defn find-active-public-evolutions
  "user-id is optional, is provided evolutions belonging to the user will be excluded"
  [db user-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.key]
                           [:e.pattern]
                           [:i.num]
                           [:e.created_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]]
                  :from   [[:evolution :e]]
                  :join   [[:iteration :i] [:= :i/evolution_id :e/id]]
                  :where  [:and
                           [:<= :i.num :e.total_iterations]
                           [:= :e.public 1]
                           [:= :i.last 1]
                           (when user-id
                             [:!= :e/user-id user-id])]}]
    (println "**" (h/format q-sqlmap))
    (sql/query db (h/format q-sqlmap) {:builder-fn sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-active-public-evolutions db nil)))
