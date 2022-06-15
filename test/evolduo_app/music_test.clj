(ns evolduo-app.music-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music :refer [chromo->abc note->abc]]))

(deftest note->abc-test
  (testing "sharps"
    (is (= "C8" (note->abc {:note 60 :total 16 :key "C"})))
    (is (= "^C8" (note->abc {:note 61 :total 16 :key "C"})))
    (is (= "^C8" (note->abc {:note 61 :total 16 :key "D"})))
    (is (= "=C8" (note->abc {:note 60 :total 16 :key "D"})))
    (is (= "^F8" (note->abc {:note 66 :total 16 :key "D"})))
    (is (= "=F8" (note->abc {:note 65 :total 16 :key "D"})))
    (is (= "^E8" (note->abc {:note 65 :total 16 :key "F#"})))
    (is (= "=E8" (note->abc {:note 64 :total 16 :key "F#"}))))
  (testing "flats"
    (is (= "C8" (note->abc {:note 60 :total 16 :key "F"})))
    (is (= "=B8" (note->abc {:note 71 :total 16 :key "F"})))
    (is (= "_G8" (note->abc {:note 66 :total 16 :key "F"})))
    (is (= "_B8" (note->abc {:note 70 :total 16 :key "F"})))
    (is (= "=G8" (note->abc {:note 67 :total 16 :key "Db"})))
    (is (= "_G8" (note->abc {:note 66 :total 16 :key "Db"})))))

(def c1 [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         ])

(deftest chromo->abc-test
  (is (= "\\\"A\\\" C8 | \\\"B\\\" D8 | \\\"C\\\" E8 | \\\"D\\\" F8" (chromo->abc c1 ["A" "B" "C" "D"] "C"))))
