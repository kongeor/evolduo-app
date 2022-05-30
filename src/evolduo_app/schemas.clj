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

(def Rating
  [:map {:closed true}
   [:chromosome_id int?]
   [:value int?]])


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(comment
  (m/validate [:re email-regex] "foo@example.com"))

(def Invitation
  [:map {:closed true}
   [:emails [:vector [:re email-regex]]]
   ])

;; TODO factor out
(defn decode-and-validate-invitation [invitation]
  (let [decoded (m/decode Invitation invitation (mt/transformer mt/default-value-transformer mt/string-transformer))]
    (if-let [error (m/explain Invitation decoded)]
      {:error (me/humanize error)}
      {:data decoded})))

;; Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character:
;; https://stackoverflow.com/questions/19605150/regex-for-password-must-contain-at-least-eight-characters-at-least-one-number-a
(def password-regex #"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$")

(def Signup
  [:and
   [:map {:closed true}
    [:email [:re {:error/message "invalid email"} email-regex]]
    [:password [:re password-regex]]
    [:password_confirmation [:string {:min 1}]]
    [:captcha [:string {:min 1}]]]
   [:fn {:error/message "passwords must match"
         :error/path [:password_confirmation]}
    (fn [{:keys [password password_confirmation]}]
          (= password password_confirmation))]])

(comment
  (me/humanize (m/explain Signup {:email                 "foo@examplecom"
                                  :password              "Pa$$word1"
                                  :password_confirmation "Pa$$word1"})))

(defn decode-and-validate [schema data]
  (let [decoded (m/decode schema data (mt/transformer mt/default-value-transformer mt/string-transformer))]
    (if-let [error (m/explain schema decoded)]
      {:error (me/humanize error)}
      {:data decoded})))

(comment
  (decode-and-validate Signup {:email "foo@example.com"
                               :password              "Pa$$word1"
                               :password_confirmation              "Pa$$word1"
                               }))


(def Subscription
  [:and
   [:map {:closed true}
    [:notifications {:default false} boolean?]
    [:announcements {:default false} boolean?]]])

(comment
  (decode-and-validate Subscription {:announcements "true"
                                     :notifications false}))

(comment
  (decode-and-validate {:chromosome_id "42"
                        :type          "rating"
                        :value         1})
  )
