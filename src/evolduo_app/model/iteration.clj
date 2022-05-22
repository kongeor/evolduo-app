(ns evolduo-app.model.iteration
  (:require [evolduo-app.model.evolution :as em]
            [honey.sql :as h]
            [next.jdbc :as jdbc]
            [evolduo-app.music :as music]
            [clojure.tools.logging :as log]
            [next.jdbc.sql :as sql])
  (:import (java.time Instant)))

(defn find-iterations-to-evolve [db]
  (let [q-sqlmap {:select [[:i/id :id] [:e/id :evolution_id]]
                  :from   [[:evolution :e]]
                  :join   [[:iteration :i] [:= :i/evolution_id :e/id]
                           [:user :u] [:= :e.user_id :u.id]]
                  :where
                  [:and
                   [:> [:raw ["strftime('%s', 'now') * 1000"]] :i.evolve_after]
                   [:>= :i.ratings :e.min_ratings]          ;; TODO <= total_iterations! mark as finished?
                   [:>= :e.total_iterations :i.num]
                   [:= :i.last 1]
                   [:= :u.deleted 0]]}]                        ;; TODO check
    (sql/query db (h/format q-sqlmap) {:builder-fn em/sqlite-builder})))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-iterations-to-evolve db)))

;; TODO duplicated, improve
(defn evolve-iteration [db settings id]
  (jdbc/with-transaction [tx db]
    (let [version (:version settings)
          old-iteration (em/find-iteration-by-id tx id)
          evolution (em/find-evolution-by-id tx (:evolution_id old-iteration))
          now (Instant/now)
          evolve-after (em/calc-evolve-after now (:evolve_after evolution))
          iter-insert (em/sql-insert! tx :iteration {:created_at now
                                                     :evolve_after evolve-after
                                                     :num (inc (:num old-iteration))
                                                     :last true
                                                     :version version
                                                     :evolution_id (:id evolution)})]
      ;; TODO meh
      (sql/update! tx :iteration (assoc old-iteration :last false) {:id (:id old-iteration)})
      (doall
        (map #(em/sql-insert! tx :chromosome
                (let [{:keys [key mode pattern chord tempo]} evolution
                      ;; TODO use strings instead
                      genes (music/random-track {:key key :measures 4 :mode (keyword mode)})
                      abc (music/->abc-track {:key   key :mode (keyword mode) :pattern pattern
                                              :chord chord :tempo tempo}
                            {:genes genes})
                      ]
                  (assoc % :iteration_id (:id iter-insert)
                           :genes (vec genes)               ;; TODO fix
                           :abc abc))) (repeat 2 em/sample-chromo))))))

(defn evolve-all-iterations [db settings]
  (let [iterations (find-iterations-to-evolve db)]
    (log/infof "Found %s iterations to evolve" (count iterations))
    ;; TODO measure times
    (doall
      (map #(do
              (log/info "Evolving iteration" (:id %))
              ;; TODO notify users
              (evolve-iteration db settings (:id %))) iterations))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)
        settings (:config/settings integrant.repl.state/system)]
    #_(assoc (em/find-iteration-by-id db 5) :last true)
    (evolve-all-iterations db settings)
    #_(em/find-iteration-chromosomes db 5)))
