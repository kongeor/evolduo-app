(ns evolduo-app.music-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music :refer :all]))

(deftest note->abc-test
  (testing "sharps"
    (is (= "C8" (note->abc {:note 60 :duration 16 :key "C"})))
    (is (= "^C8" (note->abc {:note 61 :duration 16 :key "C"})))
    (is (= "^C8" (note->abc {:note 61 :duration 16 :key "D"})))
    (is (= "=C8" (note->abc {:note 60 :duration 16 :key "D"})))
    (is (= "^F8" (note->abc {:note 66 :duration 16 :key "D"})))
    (is (= "=F8" (note->abc {:note 65 :duration 16 :key "D"})))
    (is (= "^E8" (note->abc {:note 65 :duration 16 :key "F#"})))
    (is (= "=E8" (note->abc {:note 64 :duration 16 :key "F#"}))))
  (testing "flats"
    (is (= "C8" (note->abc {:note 60 :duration 16 :key "F"})))
    (is (= "=B8" (note->abc {:note 71 :duration 16 :key "F"})))
    (is (= "_G8" (note->abc {:note 66 :duration 16 :key "F"})))
    (is (= "_B8" (note->abc {:note 70 :duration 16 :key "F"})))
    (is (= "=G8" (note->abc {:note 67 :duration 16 :key "Db"})))
    (is (= "_G8" (note->abc {:note 66 :duration 16 :key "Db"})))))

(def test-chromo [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
                  65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2])

(deftest chromo->abc-test
  (is (= "\\\"A\\\" C8 | \\\"B\\\" D8 | \\\"C\\\" E8 | \\\"D\\\" F8" (chromo->abc test-chromo ["A" "B" "C" "D"] "C"))))

(deftest get-chord-progression-notes-test
  (let [chords (gen-chord-progression-notes {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I"})]
    (is (= [[60 64 67]
            [65 69 72]
            [67 71 74]
            [60 64 67]] chords)))
  )
