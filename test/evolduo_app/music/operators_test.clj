(ns evolduo-app.music.operators-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music.operators :refer :all]))

(def c1 [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         62 -2 -2 -2 -2 -2 -2 -2 62 -2 -2 -2 -2 -2 -2 -2
         64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         ])

(deftest calc-note-length-test
  (is (= 16 (calc-note-length c1 0)))
  (is (= 16 (calc-note-length c1 48))))

(deftest split-note-test
  (is (= [60 -2 -2 -2 -2 -2 -2 -2 60 -2 -2 -2 -2 -2 -2 -2]
        (take 16 (split-note c1 0))))
  (is (= [60 -2 -2 -2 -2 -2 -2 -2 60 -2 -2 -2 60 -2 -2 -2]
        (take 16
          (-> c1
            (split-note 0)
            (split-note 8))))))

(deftest next-note-idx-test
  (is (= 16 (next-note-idx c1 0)))
  (is (nil? (next-note-idx c1 48))))

(deftest merge-note-test
  (is (= [62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2]
        (take 16
          (drop 16
            (merge-note c1 16)))))
  (is (= c1
        (merge-note c1 48))))

(deftest find-note-idxs-test
  (is (= #{0 16 24 32 48}
        (find-note-idxs c1))))

(deftest alter-random-note-pitch-test
  (with-redefs [rand-nth (constantly 0)
                rand-int (constantly 0)]
    (is (= [58 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2]
          (take 16
            (alter-random-note-pitch c1))))))

(deftest split-random-note-test
  (with-redefs [shuffle identity]
    (is (= [60 -2 -2 -2 -2 -2 -2 -2 60 -2 -2 -2 -2 -2 -2 -2]
          (take 16
            (split-random-note c1))))))

(def c2 [60 -2 -2 -2 -2 -2 -2 -2 64 -2 -2 -2 66 -2 -2 -2
         62 -2 -2 -2 -2 -2 -2 -2 62 -2 -2 -2 -2 -2 -2 -2
         64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         ])

(deftest merge-random-note-test
  (with-redefs [shuffle identity]
    (is (= [60 -2 -2 -2 -2 -2 -2 -2 64 -2 -2 -2 -2 -2 -2 -2]
          (take 16
            (merge-random-note c2))))))
