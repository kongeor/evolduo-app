(ns evolduo-app.model.evolution
  (:require [clojure.string]
            [clojure.string :as str]
            [evolduo-app.music :as music]
            [honey.sql :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [evolduo-app.music.fitness :as fitness])
  (:import (java.time Instant)
           (java.util.concurrent TimeUnit)))

(defn find-evolution-by-id
  "Given an evolution ID, return the evolution record."
  [db id]
  (sql/get-by-id db :evolutions id))

(defn find-evolution-by-id-and-user-id [db id user-id]
  (first (sql/find-by-keys db :evolutions {:id      id
                                           :user_id user-id})))

(defn get-evolutions
  [db]
  (sql/query db
    ["
select e.*
 from evolutions e
"]))

(comment
  (find-evolution-by-id (:database.sql/connection integrant.repl.state/system) 16)
  (get-evolutions (:database.sql/connection integrant.repl.state/system)))

(def sample-chromo {:fitness 42
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
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [evol-insert  (sql/insert! tx-opts :evolutions evolution)
            now          (Instant/now)
            evolve-after (calc-evolve-after now (:evolve_after evolution))
            iter-insert  (sql/insert! tx-opts :iterations {:evolve_after evolve-after
                                                      :num          0
                                                      :last         true
                                                      :version      version
                                                      :evolution_id (:id evol-insert)})]
        (doall
          (map #(sql/insert! tx-opts :chromosomes
                  (let [{:keys [key mode progression chord tempo]} evolution
                        genes (music/random-track evolution)
                        chromosome {:genes genes}
                        abc   (music/->abc-track {:key   key :mode mode :progression progression
                                                  :chord chord :tempo tempo :repetitions (:repetitions evolution)}
                                chromosome)
                        fitness (fitness/fitness evolution genes)]
                    (assoc % :iteration_id (:id iter-insert)
                             :genes (vec genes)             ;; TODO check
                             :fitness fitness
                             :abc abc))) (repeat (:population_size evolution) sample-chromo)))))))

(defn find-last-iteration-num-for-evolution [db evolution-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:i/num :num]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]]
                  :where  [:and
                           [:= :e/id evolution-id]
                           [:= :i/last true]]}]
    (-> (sql/query db (h/format q-sqlmap))
      first
      :num)))


(defn find-iteration-chromosomes [db iteration-id]
  (sql/find-by-keys db :chromosomes {:iteration_id iteration-id}))

(defn find-chromosome-by-id [db chromosome-id]
  (sql/get-by-id db :chromosomes chromosome-id))

(defn find-iteration-by-id [db iteration-id]
  (sql/get-by-id db :iterations iteration-id))

(defn increase-iteration-ratings [db iteration-id]
  (sql/query db ["update iterations set ratings = ratings + 1 where id = ?" iteration-id]))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-last-iteration-num-for-evolution db 4)
    #_(sql/query db ["select max(id) from iterations where evolution_id = ?" 2])
    #_(find-chromosome-by-id db 2)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-iteration-chromosomes db 1)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/insert! db :iterations {:num 0
                                 :evolution_id 1})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/insert! db :iterations {:num 0
                                 :evolution_id 1})))
(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/insert! db :chromosomes {:fitness 42
                                  :genes [1 2 3 4 5 -2 -2]
                                  :abc "C C C"
                                  :iteration_id 1})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/find-by-keys db :chromosomes {:iteration_id 1})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from reactions"])))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db ["select * from iterations"])))

(defn find-iteration-chromosomes [db evolution-id iteration-num]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:c.id :chromosome_id]
                           [:c.abc :abc]
                           [:c/fitness :fitness]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]
                           [:chromosomes :c] [:= :c/iteration_id :i/id]]
                  :where  [:and
                           [:= :e/id evolution-id]
                           [:= :i/num iteration-num]]}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (sql/query db (h/format q-sqlmap))))


(defn find-user-active-evolutions [db user-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.key]
                           [:e.progression]
                           [:i.num]
                           [:e.created_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]]
                  :where  [:and
                           [:= :e/user-id user-id]
                           [:<= :i.num :e.total_iterations]
                           [:= :i.last true]]}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-user-active-evolutions db 1)))

(defn find-active-public-evolutions
  "user-id is optional, is provided evolutions belonging to the user will be excluded"
  [db user-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.key]
                           [:e.progression]
                           [:i.num]
                           [:e.created_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]
                           [:users :u] [:= :e.user_id :u.id]]
                  :where  [:and
                           [:<= :i.num :e.total_iterations]
                           [:= :e.public true]
                           [:= :i.last true]
                           [:= :u.deleted false]
                           (when user-id
                             [:!= :e/user-id user-id])]}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-active-public-evolutions db nil)))

(defn find-invited-to-evolutions
  "user-id is optional, is provided evolutions belonging to the user will be excluded"
  [db user-id]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.key]
                           [:e.progression]
                           [:i.num]
                           [:e.created_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]
                           [:invitations :v] [:= :v.evolution_id :e.id]
                           [:users :u] [:= :u.id :v.invitee_id]
                           ]
                  :where  [:and
                           [:<= :i.num :e.total_iterations]
                           #_[:= :e.public 0]
                           [:= :i.last true]
                           [:= :u.id user-id]]}]
    #_(println "**" (h/format q-sqlmap))
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-invited-to-evolutions db 2)))
