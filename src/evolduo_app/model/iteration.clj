(ns evolduo-app.model.iteration
  (:require [chickn.core :as chickn]
            [chickn.math :as cmath]
            [chickn.operators :as chops]
            [chickn.util :as util]
            [clojure.math :as math]
            [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as em]
            [evolduo-app.model.rating :as rating-model]
            [evolduo-app.music :as music]
            [evolduo-app.music.stats :as stats]
            [evolduo-app.music.fitness :as fitness]
            [evolduo-app.music.operators :as mops]
            [honey.sql :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql])
  (:import (java.time Instant)))

(defn find-by-id
  [db id]
  (sql/get-by-id db :iterations id))

(defn find-by-num
  [db evolution-id num]
  (first (sql/find-by-keys db :iterations {:evolution_id evolution-id :num num})))

(defn find-iterations-to-evolve [db]
  (let [q-sqlmap {:select [[:i/id :id] [:e/id :evolution_id] [:e/key :key] [:e/mode :mode]
                           [:e/progression :progression] [:i/id :iteration_id]]
                  :from   [[:evolutions :e]]
                  :join   [[:iterations :i] [:= :i/evolution_id :e/id]
                           [:users :u] [:= :e.user_id :u.id]]
                  :where
                  [:and
                   [:> [:raw ["now()"]] :i.evolve_after]
                   [:>= :i.ratings :e.min_ratings]
                   [:> :e.total_iterations :i.num]
                   [:= :i.last true]
                   [:= :u.deleted false]]
                  :order-by [[:i.created_at :asc]]
                  :limit  20}]
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
    (find-iterations-chromosomes db 199)))

;; TODO chromosome ns ?
(defn update-fitness [db id fitness]
  (sql/update! db :chromosomes {:fitness fitness} {:id id}))

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

;; TODO put to separate ns

(defmethod chops/->operator ::music-mutation [{:keys [::chops/rate ::chops/random-func] :as cfg}]
  (fn [_ pop]
    (mapv
      (fn [c]
        (let [measures (music/chromo->measures-count (:genes c))]
          (reduce (fn [{:keys [genes] :as c} _]
                    (if (> rate (random-func))
                      (let [r (rand-int 3)]
                        (condp = r
                          0 (assoc c :genes (mops/alter-random-note-pitch genes))
                          1 (assoc c :genes (mops/merge-random-note genes))
                          2 (assoc c :genes (mops/split-random-note genes))))
                      c)) c (range (* 4 measures))))) pop))) ;; why 4? increase the mutation chance to get more diverse results

(defn chickn-evolve [evolution chromosomes]
  (let [fitness-fn  (fn [chromo]
                      (fitness/fitness evolution (fitness/maybe-fix evolution chromo)))
        cfg         #:chickn.core{:chromo-gen  #(music/random-track evolution) ;; TODO ?
                                  :pop-size    (:population_size evolution)
                                  :terminated? util/noop
                                  :monitor     util/noop
                                  :reporter    util/noop
                                  :fitness     fitness-fn
                                  :comparator  chickn/higher-is-better
                                  :selector    #:chickn.selectors{:type        :chickn.selectors/tournament
                                                                  :rate        0.3
                                                                  :random-func rand
                                                                  :tour-size   (int (* 0.75 (:population_size evolution)))
                                                                  :duplicates? false}
                                  :crossover   #:chickn.operators{:type         :chickn.operators/cut-crossover
                                                                  :rate         (float (/ (:crossover_rate evolution) 100.))
                                                                  :pointcuts    1
                                                                  :rand-nth     rand-nth
                                                                  :random-point cmath/rnd-index
                                                                  :random-func  rand}
                                  :mutation    #:chickn.operators{:type        ::music-mutation
                                                                  :rate        (float (/ (:mutation_rate evolution) 100.))
                                                                  :random-func rand}
                                  :reinsertion #:chickn.reinsertion{:type :chickn.reinsertion/elitist
                                                                    :rate 0.1}}
        ]
    (:pop (:genotype (chickn/evolve cfg {:pop chromosomes :iteration 0} 1)))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)
        evolution (em/find-evolution-by-id db 32)
        chromosomes (find-iterations-chromosomes db 60)
        ;; debug stuff
        c' (update-in chromosomes [0 :genes] assoc 0 -2)]
    (chickn-evolve evolution c')
    #_(map #(count (:genes %)) (find-iterations-chromosomes db 60))))

(comment
  (music/random-track {:key "C" :measures 4 :mode "major" :repetitions 1})
  #_(mapv (fn [g] {:genes g :fitness -42 :age 0}) (repeat 10 music/c1)))

(defn calc-adjusted-fitness [ratings stats chromosome]
  (let [chromosome-rating (get ratings (:id chromosome) 0)]
    (math/round
      (+ (:fitness chromosome)
         (if stats                                          ;; backwards compatibility
           (let [{:keys [min max]} stats
                 *diff (/ (- max min) 4)]
             (*
               chromosome-rating
               (if (< *diff 5)
                 10
                 *diff)))
           (* 0.2
              chromosome-rating
              (abs (:fitness chromosome))))))))

(defn adjust-iteration-chromosome-fitness [ratings stats chromos]
  (let [calc-fn (partial calc-adjusted-fitness ratings stats)]
    (mapv #(assoc % :raw_fitness (:fitness %)
                    :fitness (calc-fn %)) chromos)))

;; TODO duplicated, improve
(defn evolve-iteration [db settings id]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [version       (:version settings)
            old-iteration (em/find-iteration-by-id tx-opts id)
            iter-chromos  (find-iterations-chromosomes db id)
            evolution     (em/find-evolution-by-id tx-opts (:evolution_id old-iteration))
            ratings       (rating-model/find-iteration-ratings db (:evolution_id old-iteration) (:num old-iteration))
            ;; there is no way to know upfront the adjusted fitness, users need to submit their ratings first
            ;; this is why this update happens at this stage. This is far from ideal, but should be all right
            ;; for now. There will be no easy way to debug user ratings unless this information is present in the db
            iter-chromos' (adjust-iteration-chromosome-fitness ratings (:stats old-iteration) iter-chromos)
            _             (doseq [c iter-chromos']
                            (update-fitness db (:id c) (:fitness c)))
            now           (Instant/now)
            evolve-after  (em/calc-evolve-after now (:evolve_after evolution))
            new-chromos   (chickn-evolve evolution iter-chromos')
            stats         (stats/compute-iteration-stats new-chromos)
            maybe-fix-fn  (partial fitness/maybe-fix evolution)
            new-chromos'  (mapv #(update % :genes maybe-fix-fn) new-chromos)
            iter-insert   (sql/insert! tx-opts :iterations {:evolve_after evolve-after
                                                            :num          (inc (:num old-iteration))
                                                            :last         true
                                                            :version      version
                                                            :evolution_id (:id evolution)
                                                            :stats        stats})]
        ;; TODO meh
        (sql/update! tx-opts :iterations (assoc old-iteration :last false) {:id (:id old-iteration)})
        (doall
          (map #(sql/insert! tx-opts :chromosomes
                  (let [abc   (music/->abc-track evolution %)]
                    (assoc (select-keys % [:genes :fitness])
                      :iteration_id (:id iter-insert)
                      :raw_fitness (:fitness %)
                      :abc abc))) new-chromos'))))))

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
