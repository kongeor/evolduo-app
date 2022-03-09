(ns evolduo-app.schemas
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]))

(def music-keys #{"C" "D"})
(def patterns #{"I-IV-V-I"})


(def Evolution
  [:map {:closed true}
   [:public {:default false} boolean?]
   [:min_ratings int?]
   [:initial_iterations int?]
   [:total_iterations int?]
   [:crossover_rate int?]
   [:mutation_rate int?]
   [:key (vec (cons :enum music-keys))]
   [:pattern (vec (cons :enum patterns))]
   [:tempo int?]])

(def example-evolution {:min_ratings        "5"
                        :initial_iterations 10
                        :total_iterations   20
                        :crossover_rate     30
                        :mutation_rate      5
                        :key                "D"
                        :pattern            "I-IV-V-I"
                        :tempo              60})

(comment
  (m/explain Evolution example-evolution))

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