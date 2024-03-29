(ns evolduo-app.schemas
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]
            [evolduo-app.music :as music]
            [evolduo-app.music.accompaniment :as acco]
            [evolduo-app.music.midi :as midi]
            [clojure.string :as str]))

;; TODO labels
;; only admin immediately
(def evolve-after-options ["1-min" "5-min" "30-min" "8-hour" "1-day"])
(def repetition-options [1 2 3 4])

(def Evolution
  [:and
   [:map {:closed true}
    [:public {:default false} boolean?]
    [:min_ratings [:int {:min 0 :max 20}]]
    [:evolve_after (vec (cons :enum evolve-after-options))]
    [:initial_iterations int?]
    [:total_iterations [:int {:min 10 :max 50}]]
    [:population_size [:int {:min 10 :max 40}]]
    [:crossover_rate [:int {:min 1 :max 100}]]
    [:mutation_rate [:int {:min 1 :max 100}]]
    [:mode (vec (cons :enum music/mode-names))]
    [:key (vec (cons :enum music/music-keys-restricted))]
    [:progression (vec (cons :enum music/progressions))]
    [:repetitions [:int {:min 1 :max 4}]]
    [:chord (vec (cons :enum music/chord-intervals-keys))]
    [:tempo [:int {:min 40 :max 220}]]
    [:accompaniment (vec (cons :enum acco/pattern-keys))]
    [:instrument [:and :int (vec (cons :enum midi/instrument-keys))]]]
   [:fn {:error/message "invalid total number of iterations"
         :error/path [:total_iterations]}
    (fn [{:keys [total_iterations]}]
      (= (mod total_iterations 10) 0))]
   [:fn {:error/message "invalid total population size"
         :error/path [:population_size]}
    (fn [{:keys [population_size]}]
      (= (mod population_size 10) 0))]])

(def example-evolution {:public             true
                        :min_ratings        "5"
                        :evolve_after       "5-min"
                        :initial_iterations 10
                        :total_iterations   "20"
                        :population_size    "10"
                        :crossover_rate     30
                        :mutation_rate      5
                        :mode               "major"
                        :key                "C"
                        :chord              "R + 3 + 3"
                        :progression        "I-IV-V-I"
                        :repetitions        "1"
                        :tempo              60
                        :accompaniment      "fixed"
                        :instrument         "4"})

(comment
  (me/humanize
    (m/explain Evolution
      (m/decode Evolution example-evolution mt/string-transformer))))

(comment
  (decode-and-validate Evolution example-evolution))
;; TODO split schemas

(def Rating
  [:map {:closed true}
   [:chromosome_id int?]
   [:value [:int {:min -2 :max 2}]]])


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
(def password-regex #"^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$")

(defn safe-lower-case [s]
  (when s
    (str/lower-case s)))

(def Signup
  [:and
   [:map {:closed true}
    [:email {:decode/string {:enter safe-lower-case}} [:re {:error/message "invalid email"} email-regex]]
    [:password [:re {:error/message "Invalid password"} password-regex]]
    [:password_confirmation [:string]]
    [:captcha [:string {:min 1}]]
    [:newsletters {:optional true} [:string]]]
   [:fn {:error/message "passwords must match"
         :error/path [:password_confirmation]}
    (fn [{:keys [password password_confirmation]}]
          (= password password_confirmation))]])

(def Login
  [:and
   [:map {:closed true}
    [:email {:decode/string {:enter safe-lower-case}} [:re {:error/message "invalid email"} email-regex]]
    [:password [:re {:error/message "Invalid password"} password-regex]]]])

(comment
  (me/humanize (m/explain Signup {:email                 "foo@examplecom"
                                  :password              "Foo123456"
                                  :password_confirmation ""})))

(defn decode-and-validate [schema data]
  (let [decoded (m/decode schema data (mt/transformer mt/default-value-transformer mt/string-transformer))]
    (if-let [error (m/explain schema decoded)]
      {:error (me/humanize error)}
      {:data decoded})))

(comment
  (decode-and-validate Signup {:email "FOO@example.com"
                               :password              "Pa$$word1"
                               :password_confirmation              "Pa$$word1"
                               :captcha "foo"
                               }))


(def Subscription
  [:and
   [:map {:closed true}
    [:notifications {:default false} boolean?]
    [:announcements {:default false} boolean?]]])

(comment
  (decode-and-validate Subscription {:announcements "bananes"
                                     :notifications false}))

(comment
  (decode-and-validate Rating {:chromosome_id "42"
                               :value         1})
  )

(def PasswordReset
  [:map {:closed true}
    [:email [:re {:error/message "invalid email"} email-regex]]
    [:captcha [:string {:min 1}]]])

(def PasswordSet
  [:and
   [:map {:closed true}
    [:token [:string {:min 1}]]
    [:password [:re {:error/message "Invalid password"} password-regex]]
    [:password_confirmation [:string]]]
   [:fn {:error/message "passwords must match"
         :error/path [:password_confirmation]}
    (fn [{:keys [password password_confirmation]}]
          (= password password_confirmation))]])

(def post-actions
  [["save-draft" "Save Draft"]
   ["save-and-send-test" "Save Draft and Send Test Email"]
   ["publish" "Publish"]
   ["publish-and-send-emails" "Publish and Send Announcement"]])

(def NewsPost
  [:and
   [:map {:closed true}
    [:title [:string {:min 1}]]
    [:content [:string {:min 1}]]
    [:action (vec (cons :enum (mapv first post-actions)))]]])
