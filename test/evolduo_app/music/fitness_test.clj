(ns evolduo-app.music.fitness-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music.fitness :refer :all]))

(def invalid
  [64 -2 -2 -2 69 -2 -2 -2 62 -2 -2 -2 60 -2 -2 -2
   -2 -2 -2 -2 67 -2 -2 -2 62 -2 -2 -2 67 -2 -2 -2])

(deftest valid?-test
  (is (not (valid? invalid))))

(deftest maybe-fix-test
  (is (= (assoc invalid 16 60)
        (maybe-fix {:key "C"} invalid))))