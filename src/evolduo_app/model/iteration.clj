(ns evolduo-app.model.iteration
  (:require [evolduo-app.model.evolution :as em]
            [honey.sql :as h]
            [next.jdbc :as jdbc]
            [evolduo-app.music :as music]
            [clojure.tools.logging :as log]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs])
  (:import (java.time Instant)))

(defn find-by-id
  [db id]
  (sql/get-by-id db :iterations id))

(defn find-by-num
  [db num]
  (first (sql/find-by-keys db :iterations {:num num})))

(defn find-iterations-to-evolve [db]
  (let [q-sqlmap {:select [[:i/id :id] [:e/id :evolution_id]]
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

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-iterations-to-evolve db)))

;; TODO duplicated, improve
(defn evolve-iteration [db settings id]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [version       (:version settings)
            old-iteration (em/find-iteration-by-id tx-opts id)
            evolution     (em/find-evolution-by-id tx-opts (:evolution_id old-iteration))
            now           (Instant/now)
            evolve-after  (em/calc-evolve-after now (:evolve_after evolution))
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
                        ;; TODO use strings instead
                        genes (music/random-track {:key key :measures 4 :mode (keyword mode)})
                        abc   (music/->abc-track {:key   key :mode (keyword mode) :progression progression
                                                  :chord chord :tempo tempo}
                                {:genes genes})
                        ]
                    (assoc % :iteration_id (:id iter-insert)
                             :genes (vec genes)             ;; TODO fix
                             :abc abc))) (repeat 2 em/sample-chromo)))))))

(defn evolve-all-iterations [db settings]
  (let [iterations (find-iterations-to-evolve db)]
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
