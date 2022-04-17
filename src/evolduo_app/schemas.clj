(ns evolduo-app.schemas
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]
            [evolduo-app.music :as music]))

;; TODO labels
;; only admin immediately
(def evolve-after-options ["1-min" "5-min" "30-min" "8-hour" "1-day"])

(def Evolution
  [:map {:closed true}
   [:public {:default false} boolean?]
   [:min_ratings int?]
   [:evolve_after (vec (cons :enum evolve-after-options))]
   [:initial_iterations int?]
   [:total_iterations int?]
   [:population_size int?]
   [:crossover_rate int?]
   [:mutation_rate int?]
   [:mode (vec (cons :enum music/modes))]
   [:key (vec (cons :enum music/music-keys))]
   [:pattern (vec (cons :enum music/patterns))]
   [:chord (vec (cons :enum music/chord-intervals-keys))]
   [:tempo int?]])

(def example-evolution {:min_ratings        "5"
                        :evolve_after       "5-min"
                        :initial_iterations 10
                        :total_iterations   20
                        :population_size    10
                        :crossover_rate     30
                        :mutation_rate      5
                        :mode               "major"
                        :key                "D"
                        :chord              "R + 3 + 3"
                        :pattern            "I-IV-V-I"
                        :tempo              60})

(comment
  (me/humanize (m/explain Evolution example-evolution)))

(comment
  (m/decode Evolution {:public      "true"
                       :min_ratings "2"} mt/string-transformer))

(defn decode-and-validate-evolution [evolution]
  (let [decoded (m/decode Evolution evolution (mt/transformer mt/default-value-transformer mt/string-transformer))]
    (if-let [error (m/explain Evolution decoded)]
      {:error (me/humanize error)}
      {:data decoded})))

(comment
  (decode-and-validate-evolution example-evolution))

;; TODO split schemas

(def Reaction
  [:map {:closed true}
   [:chromosome_id int?]
   [:type string?]                                      ;; TODO enum
   [:value int?]])

(defn decode-and-validate-reaction [reaction]
  (let [decoded (m/decode Reaction reaction (mt/transformer mt/default-value-transformer mt/string-transformer))]
    (if-let [error (m/explain Reaction decoded)]
      {:error (me/humanize error)}
      {:data decoded})))

(comment
  (decode-and-validate-reaction {:chromosome_id "42"
                                 :type          "rating"
                                 :value         1})
  )
