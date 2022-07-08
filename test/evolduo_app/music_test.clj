(ns evolduo-app.music-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music :refer :all]))

(deftest note->abc-test
  (testing "sharps"
    (is (= ["C8" #{}] (note->abc {:note 60 :duration 16 :key "C"})))
    (is (= ["^C8" #{1}] (note->abc {:note 61 :duration 16 :key "C" :sharp? true})))
    (is (= ["C8" #{1 6}] (note->abc {:note 61 :duration 16 :key "D" :sharp? true} {:sharps #{6 1}})))
    (is (= ["=C8" #{6}] (note->abc {:note 60 :duration 16 :key "D" :sharp? true} {:sharps #{6 1}})))
    (is (= ["F8" #{1 6}] (note->abc {:note 66 :duration 16 :key "D" :sharp? true} {:sharps #{6 1}})))
    (is (= ["=F8" #{1}] (note->abc {:note 65 :duration 16 :key "D" :sharp? true} {:sharps #{6 1}})))
    (is (= ["=F8" #{1 10 3 5 8}] (note->abc {:note 65 :duration 16 :key "F#" :sharp? true} {:sharps #{6 1 8 3 10 5}})))
    (is (= ["=E8" #{1 10 3 6 8}] (note->abc {:note 64 :duration 16 :key "F#" :sharp? true} {:sharps #{6 1 8 3 10 5}}))))
  (testing "flats"
    (is (= ["C8" #{10}] (note->abc {:note 60 :duration 16 :key "F"} {:flats #{10}})))
    (is (= ["=B8" #{}] (note->abc {:note 71 :duration 16 :key "F"} {:flats #{10}})))
    (is (= ["_G8" #{10 6}] (note->abc {:note 66 :duration 16 :key "F"} {:flats #{10}})))
    (is (= ["B8" #{10}] (note->abc {:note 70 :duration 16 :key "F"} {:flats #{10}})))
    (is (= ["G8" #{1 10 3 8}] (note->abc {:note 67 :duration 16 :key "Db"} {:flats #{10 3 8 1}})))
    (is (= ["_G8" #{1 10 3 6 8}] (note->abc {:note 66 :duration 16 :key "Db"} {:flats #{10 3 8 1}})))))

(def test-chromo [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2])

(def test-chromo-2 [61 -2 -2 -2
                    62 -2 -2 -2
                    62 -2 -2 -2
                    61 -2 -2 -2 ])

(def test-chromo-3 [63 -2 -2 -2
                    62 -2 -2 -2
                    62 -2 -2 -2
                    63 -2 -2 -2 ])

(deftest chromo->abc-test
  (testing "just a C major"
    (is (= "\\\"A\\\" C8 | \\\"B\\\" D8 | \\\"C\\\" E8 | \\\"D\\\" F8" (chromo->abc test-chromo ["A" "B" "C" "D"] "C" "major"))))
  (testing "sharps"
    (is (= (chromo->abc test-chromo-3 ["A"] "A" "major") "\\\"A\\\" ^D2 =D2 D2 ^D2")))
  (testing "flats"
    (is (= (chromo->abc test-chromo-2 ["A"] "Bb" "major") "\\\"A\\\" _D2 =D2 D2 _D2"))))

(deftest get-chord-progression-notes-test
  (let [chords (gen-chord-progression-notes {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I"})]
    (is (= [[60 64 67]
            [65 69 72]
            [67 71 74]
            [60 64 67]] chords))))
