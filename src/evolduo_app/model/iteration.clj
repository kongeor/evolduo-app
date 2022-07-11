(ns evolduo-app.model.iteration
  (:require [evolduo-app.model.evolution :as em]
            [honey.sql :as h]
            [next.jdbc :as jdbc]
            [evolduo-app.music :as music]
            [evolduo-app.music.fitness :as fitness]
            [clojure.tools.logging :as log]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [chickn.core :as chickn]
            [chickn.math :as cmath]
            [chickn.util :as util]
            [chickn.operators :as chops]
            [evolduo-app.music.operators :as mops])
  (:import (java.time Instant)))

(defn find-by-id
  [db id]
  (sql/get-by-id db :iterations id))

(defn find-by-num
  [db num]
  (first (sql/find-by-keys db :iterations {:num num})))

(defn find-iterations-to-evolve [db]
  (let [q-sqlmap {:select [[:i/id :id] [:e/id :evolution_id] [:e/key :key] [:e/mode :mode]
                           [:e/progression :progression] [:i/id :iteration_id]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]
                           [:users :u] [:= :e.user_id :u.id]]
                  :where
                  [:and
                   [:> [:raw ["now()"]] :i.evolve_after]
                   [:>= :i.ratings :e.min_ratings]          ;; TODO <= total_iterations! mark as finished?
                   [:> :e.total_iterations :i.num]
                   [:= :i.last true]
                   [:= :u.deleted false]]}]                        ;; TODO check
    (sql/query db (h/format q-sqlmap))))

(defn find-iterations-chromosomes [db iteration-id]
  (let [q-sqlmap {:select [[:c/id :id] [:c/genes :genes] [:c/fitness :fitness]]
                  :from   [[:iterations :i]]
                  :join   [[:chromosomes :c] [:= :c.iteration_id :i.id]]
                  :where
                  [:and
                   [:= :i.id iteration-id]
                   ]}]
    (sql/query db (h/format q-sqlmap))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    #_(find-by-id db 1)
    #_(find-iterations-to-evolve db)
    (find-iterations-chromosomes db 1)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)
        iter (first (find-iterations-to-evolve db))
        iter' (assoc iter :mode (keyword (:mode iter)))               ;; TODO fix keyword

        ]
    (fitness/fitness iter' (first (find-iterations-chromosomes db 1)))
    ))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    #_(em/find-evolution-by-id db 32)
    #_(em/find-iteration-chromosomes db 32 2)
    (map :genes (take 2 (find-iterations-chromosomes db 60)))
    #_(map #(count (:genes %)) (find-iterations-chromosomes db 60))))

(defmethod chops/->operator ::music-mutation [{:keys [::chops/rate ::chops/random-func] :as cfg}]
  (fn [_ pop n]
    (mapv
      (fn [{:keys [genes] :as c}]
        (if (> rate (random-func))
          (assoc c :genes (mops/alter-random-note-pitch genes))
          c)) pop)))

(defn- chickn-evolve [evolution chromosomes]
  (let [fitness-fn  (fn [chromo]
                      (fitness/fitness evolution chromo))
        cfg         #:chickn.core{:chromo-gen #(music/random-track evolution)
                                  :pop-size    (count chromosomes)
                                  :terminated? (constantly false)
                                  :monitor     util/noop
                                  :reporter    util/noop
                                  :fitness     fitness-fn
                                  :comparator  chickn/higher-is-better
                                  :selectors   [#:chickn.selectors{:type :chickn.selectors/best
                                                                   :elit true
                                                                   :rate 0.1
                                                                   :random-func rand}
                                                #:chickn.selectors{:type        :chickn.selectors/roulette
                                                                   :rate        0.3
                                                                   :random-func rand}]
                                  :operators   [#:chickn.operators{:type         :chickn.operators/cut-crossover
                                                                   :rate         0.3
                                                                   :pointcuts    1
                                                                   :rand-nth     rand-nth
                                                                   :random-point rand-nth
                                                                   :random-func  rand}
                                                #:chickn.operators{:type        ::music-mutation
                                                                   :rate        (float (/ (:mutation_rate evolution) 100.))
                                                                   :random-func rand}]}
        ]
    (:pop (:genotype (chickn/evolve cfg {:pop chromosomes :iteration 0} 1)))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)
        evolution (em/find-evolution-by-id db 32)
        chromosomes (find-iterations-chromosomes db 60)]
    (chickn-evolve evolution chromosomes)
    #_(map #(count (:genes %)) (find-iterations-chromosomes db 60))))

(comment
  (music/random-track {:key "C" :measures 4 :mode "major" :repetitions 1})
  #_(mapv (fn [g] {:genes g :fitness -42 :age 0}) (repeat 10 music/c1)))

;; TODO duplicated, improve
(defn evolve-iteration [db settings id]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [version       (:version settings)
            old-iteration (em/find-iteration-by-id tx-opts id)
            iter-chromos  (find-iterations-chromosomes db id)
            evolution     (em/find-evolution-by-id tx-opts (:evolution_id old-iteration))
            now           (Instant/now)
            evolve-after  (em/calc-evolve-after now (:evolve_after evolution))
            new-chromos   (chickn-evolve evolution iter-chromos)
            iter-insert   (sql/insert! tx-opts :iterations {:evolve_after evolve-after
                                                       :num          (inc (:num old-iteration))
                                                       :last         true
                                                       :version      version
                                                       :evolution_id (:id evolution)})]
        ;; TODO meh
        (sql/update! tx-opts :iterations (assoc old-iteration :last false) {:id (:id old-iteration)})
        (doall
          (map #(sql/insert! tx-opts :chromosomes
                  (let [{:keys [key mode progression chord tempo]} evolution
                        abc   (music/->abc-track {:key   key :mode mode :progression progression
                                                  :chord chord :tempo tempo :repetitions (:repetitions evolution)}
                                %)]
                    (assoc (select-keys % [:genes :fitness])
                      :iteration_id (:id iter-insert)
                      :abc abc))) new-chromos))))))

(defn evolve-all-iterations [db settings]
  (let [iterations (find-iterations-to-evolve db)]
    ;; TODO measure times
    (doall
      (map #(do
              (log/infof "Evolving iteration %s for evolution %s" (:id %) (:evolution_id %))
              ;; TODO notify users
              (evolve-iteration db settings (:id %))) iterations))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)
        settings (:config/settings integrant.repl.state/system)]
    #_(assoc (em/find-iteration-by-id db 5) :last true)
    (evolve-all-iterations db settings)
    #_(em/find-iteration-chromosomes db 5)))
