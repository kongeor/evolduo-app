(ns evolduo-app.music.fitness-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music.fitness :refer :all]))


(def test-chromo [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  67 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2])

(def test-settings {:key         "C" :mode "major" :duration 8
                    :progression "I-IV-V-I" :chord "R + 3 + 3"
                    :repetitions 1})

(deftest last-note-scores-test
  (is (= [80 80 80 80]
        (mapv calc-last-note-score (analyze test-settings test-chromo))))
  (is (= [0 0 40.0 80]
         (mapv calc-last-note-score (analyze (assoc test-settings :progression "II-V-I-I") test-chromo)))))

(deftest fitness-test
  (with-redefs [calc-last-notes-score (constantly 0)
                calc-scale-score      (constantly 0)]
    (testing "zero note miss"
      (is (= 0 (fitness test-settings test-chromo))))
    (testing "changing to a 3rd is fine"
      (is (= 0 (fitness test-settings (assoc test-chromo 0 64)))))
    (testing "changing to a 5th is also fine"
      (is (= 0 (fitness test-settings (assoc test-chromo 0 67)))))
    (testing "changing to a 7th is not"
      (is (= -16 (fitness test-settings (assoc test-chromo 0 71)))))
    (testing "unless we have a 7th chord"
      (is (= 0 (fitness (assoc test-settings :chord "R + 3 + 3 + 3")
                 (assoc test-chromo 0 71)))))
    (testing "penalty is proportional to the note length"
      (is (= -8 (fitness test-settings (assoc test-chromo 0 71 8 60))))))
  )

(def invalid
  [64 -2 -2 -2 69 -2 -2 -2 62 -2 -2 -2 60 -2 -2 -2
   -2 -2 -2 -2 67 -2 -2 -2 62 -2 -2 -2 67 -2 -2 -2])

(deftest valid?-test
  (is (not (valid? invalid))))

(deftest maybe-fix-test
  (is (= (assoc invalid 16 60)
        (maybe-fix {:key "C"} invalid))))