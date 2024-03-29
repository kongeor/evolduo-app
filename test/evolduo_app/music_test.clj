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


(deftest transpose-key-test
  (is (= "C" (transpose-key "D" "dorian")))
  (is (= "Db" (transpose-key "F" "phrygian")))
  (is (= "D" (transpose-key "C#" "locrian")))
  (is (= "Eb" (transpose-key "Ab" "lydian")))
  (is (= "E" (transpose-key "C#" "minor")))
  (is (= "F" (transpose-key "G" "dorian")))
  (is (= "Gb" (transpose-key "Eb" "minor")))
  (is (= "G" (transpose-key "F#" "locrian")))
  (is (= "G" (transpose-key "F#" "locrian")))
  (is (= "B" (transpose-key "F#" "mixolydian")))

  ;; Db phrygian gives no accidentals in the abc.js editor, but it does
  ;; transpose to Bb Maj which has 4 flats
  (is (= "Bb" (transpose-key "Db" "phrygian")))

  ;; not mapped, Eb locrian will result into E locrian which transposes to F
  ;; not sure if that's accurate, but this is what abc.js seems to do
  (is (= "F" (transpose-key "Eb" "locrian")))
  )

(def test-chromatic-measure [60 -2 61 -2 62 -2 63 -2
                             64 -2 65 -2 66 -2 67 -2
                             68 -2 69 -2 70 -2 71 -2
                             72 -2])

(deftest measure->abc-test
  (testing "asdf"
    (is (= "C ^C D ^D E F ^F G ^G A ^A B =c"
           (measure->abc test-chromatic-measure "C" "major"))))
  (testing "asdf"
    (is (= "C D =D E =E F G =G A =A B =B c"
           (measure->abc test-chromatic-measure "Db" "major")))))


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
    (is (= "\\\"A\\\" C8 | \\\"B\\\" D8 | \\\"C\\\" E8 | \\\"D\\\" F8"
           (chromo->abc test-chromo ["A" "B" "C" "D"] "C" "major"))))
  (testing "sharps"
    (is (= "\\\"A\\\" ^D2 =D2 D2 ^D2"
           (chromo->abc test-chromo-3 ["A"] "A" "major"))))
  (testing "flats"
    (is (= "\\\"A\\\" _D2 =D2 D2 _D2"
           (chromo->abc test-chromo-2 ["A"] "Bb" "major")))))

(deftest get-chord-progression-notes-test
  (let [chords (gen-chord-progression-notes {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1})]
    (is (= [[60 64 67]
            [65 69 72]
            [67 71 74]
            [60 64 67]] chords))))

(deftest calc-note-times-test
  (testing "note times"
    (is (= [{:duration 16
             :note     60
             :index    0}
            {:duration 16
             :note     62
             :index    16}
            {:duration 16
             :note     64
             :index    32}
            {:duration 16
             :note     65
             :index    48}] (calc-note-times test-chromo)))))
