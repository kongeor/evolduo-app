(ns evolduo-app.model.evolution
  (:require [clojure.string]
            [clojure.string :as str]
            [evolduo-app.music :as music]
            [evolduo-app.music.stats :as stats]
            [honey.sql :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [evolduo-app.music.fitness :as fitness]
            [evolduo-app.music.midi :as midi])
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

(defn num-of-evolutions-in-last-day [db user-id]
  (let [q-sqlmap {:select [[[:raw "count(*)"] :count]]
                  :from   [[:evolutions :e]]
                  :where
                  [:and
                   [:> :e.created_at [:raw ["now() - interval '1 day'"]]]
                   [:= :e.user_id user-id]]}]
    (:count (first (sql/query db (h/format q-sqlmap))))))

(comment
  (num-of-evolutions-in-last-day (:database.sql/connection integrant.repl.state/system) 1))

(defn generate-initial-chromosomes [{:keys [key mode progression chord tempo
                                             population_size repetitions] :as evolution}]
  (repeatedly population_size
              #(let [genes      (music/random-track evolution)
                     chromosome {:genes genes}
                     abc        (music/->abc-track evolution chromosome)
                     fitness    (fitness/fitness evolution genes)]
                 {:genes       (vec genes)                  ;; TODO check
                  :fitness     fitness
                  :raw_fitness fitness
                  :abc         abc})))

(defn save-evolution
  [db {:keys [version]} evolution]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [evol-insert  (sql/insert! tx-opts :evolutions evolution)
            now          (Instant/now)
            evolve-after (calc-evolve-after now (:evolve_after evolution))
            chromos      (generate-initial-chromosomes evolution)
            stats        (stats/compute-iteration-stats chromos)
            iter-insert  (sql/insert! tx-opts :iterations {:evolve_after evolve-after
                                                           :num          0
                                                           :last         true
                                                           :version      version
                                                           :stats        stats
                                                           :evolution_id (:id evol-insert)})
            ]
        (doall
          (map #(sql/insert! tx-opts :chromosomes
                             (assoc % :iteration_id (:id iter-insert))) chromos))
        evol-insert))))

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
                           [:c/fitness :fitness]
                           [:c/raw_fitness :raw_fitness]]
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


(defn find-user-active-evolutions [db user-id & {:keys [limit] :or {limit 10}}]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.min_ratings]
                           [:e.evolve_after]
                           [:e.total_iterations]
                           [:e.population_size]
                           [:e.crossover_rate]
                           [:e.mutation_rate]
                           [:e.key]
                           [:e.mode]
                           [:e.progression]
                           [:e.repetitions]
                           [:e.chord]
                           [:e.tempo]
                           [:i.num]
                           [:e.created_at]
                           [:i.created_at :updated_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]
                           [:e.user_id :user_id]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]]
                  :where  [:and
                           [:= :e/user-id user-id]
                           [:<= :i.num :e.total_iterations]
                           [:= :i.last true]]
                  :order-by [[:i.created_at :desc]]
                  :limit  limit}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-user-active-evolutions db 1)))

(defn find-active-public-evolutions
  "user-id is optional, is provided evolutions belonging to the user will be excluded"
  [db user-id & {:keys [limit] :or {limit 10}}]
  (let [q-sqlmap {:select   [[:e/id :evolution_id]
                             [:i/id :iteration_id]
                             [:e.min_ratings]
                             [:e.evolve_after]
                             [:e.total_iterations]
                             [:e.population_size]
                             [:e.crossover_rate]
                             [:e.mutation_rate]
                             [:e.key]
                             [:e.mode]
                             [:e.progression]
                             [:e.repetitions]
                             [:e.chord]
                             [:e.tempo]
                             [:i.num]
                             [:e.created_at]
                             [:i.created_at :updated_at]
                             [:e.total_iterations]
                             [[:- :e.total_iterations :i.num] :iterations_to_go]
                             [:e.user_id :user_id]]
                  :from     [[:evolutions :e]]
                  :join     [[:iterations :i] [:= :i/evolution_id :e/id]
                             [:users :u] [:= :e.user_id :u.id]]
                  :where    [:and
                             [:<= :i.num :e.total_iterations]
                             [:= :e.public true]
                             [:= :i.last true]
                             [:= :u.deleted false]
                             (when user-id
                               [:!= :e/user-id user-id])]
                  :order-by [[:i.created_at :desc]]
                  :limit    10}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-active-public-evolutions db nil)))

(defn find-invited-to-evolutions
  "user-id is optional, is provided evolutions belonging to the user will be excluded"
  [db user-id & {:keys [limit] :or {limit 10}}]
  (let [q-sqlmap {:select [[:e/id :evolution_id]
                           [:i/id :iteration_id]
                           [:e.min_ratings]
                           [:e.evolve_after]
                           [:e.total_iterations]
                           [:e.population_size]
                           [:e.crossover_rate]
                           [:e.mutation_rate]
                           [:e.key]
                           [:e.mode]
                           [:e.progression]
                           [:e.repetitions]
                           [:e.chord]
                           [:e.tempo]
                           [:i.num]
                           [:e.created_at]
                           [:i.created_at :updated_at]
                           [:e.total_iterations]
                           [[:- :e.total_iterations :i.num] :iterations_to_go]
                           [:e.user_id :user_id]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]
                           [:invitations :v] [:= :v.evolution_id :e.id]
                           [:users :u] [:= :u.id :v.invitee_id]
                           ]
                  :where  [:and
                           [:<= :i.num :e.total_iterations]
                           #_[:= :e.public 0]
                           [:= :i.last true]
                           [:= :u.id user-id]]
                  :order-by [[:i.created_at :desc]]
                  :limit   10}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-invited-to-evolutions db 2)))

(defn- rnd-standard-progression []
  (rand-nth
    [{:progression "I-V-VI-III-IV-I-IV-V"
      :repetitions 2}
     {:progression "I-I-I-I-IV-IV-I-I-V-V-I-I"
      :repetitions 2}
     {:progression "VI-II-V-I"
      :repetitions 4}
     {:progression "I-IV-V-I"
      :repetitions 4}
     ]))

(def default-evolution-params {:public             true
                               :min_ratings        1
                               :evolve_after       "5-min"
                               :initial_iterations 0
                               :total_iterations   20
                               :population_size    20
                               :crossover_rate     20
                               :mutation_rate      30
                               :key                "C"
                               :mode               "major"
                               :progression        "I-IV-V-I"
                               :repetitions        2
                               :chord              "R + 3 + 3"
                               :tempo              130
                               :accompaniment      "fixed"
                               :instrument         4})

(defn preset->params [is-admin? preset]
  (let [p
        (condp = preset
          "minimal"
          {:crossover_rate   20
           :mutation_rate    30
           :total_iterations 20
           :progression      (rand-nth ["I-I-I-I" "I-IV-I-IV"])
           :mode             (rand-nth ["major" "dorian" "mixolydian" "minor"])
           :chord            (rand-nth ["R" "R + 5 + R"])
           :tempo            100}
          "progressive"
          {:crossover_rate   20
           :mutation_rate    40
           :total_iterations 20
           :repetitions      4
           :progression      (rand-nth ["II-V-I-I" "I-IV-II-V"])
           :mode             (rand-nth ["dorian" "mixolydian" "lydian"])
           :chord            "R + 3 + 3 + 3"
           :tempo            130}
          "experimental"
          {:crossover_rate   10
           :mutation_rate    90
           :total_iterations 20
           :repetitions      4
           :progression      (rand-nth ["V-III-V-III" "I-I-VII-I"])
           :mode             (rand-nth ["phrygian" "lydian" "locrian"])
           :chord            "R + 3 + 3 + 3"
           :tempo            130}
          (merge
            default-evolution-params
            (rnd-standard-progression)
            {:mode (rand-nth ["major" "mixolydian" "minor"])}))]
    (cond-> (merge
              default-evolution-params
              p
              {:instrument (rand-nth midi/rnd-instrument-keys)})
            is-admin?
            (assoc :min_ratings 0 :evolve_after "1-min" :total_iterations 40))))

(comment
  (preset->params false "default"))
