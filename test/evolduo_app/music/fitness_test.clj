(ns evolduo-app.music.fitness-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music.fitness :refer [valid?]]))

(def invalid
  [64 -2 -2 -2 69 -2 -2 -2 62 -2 -2 -2 60 -2 -2 -2
   60 -2 -2 -2 71 -2 71 -2 64 -2 -2 -2 69 -2 -2 -2
   -2 -2 -2 -2 67 -2 -2 -2 62 -2 -2 -2 67 -2 -2 -2
   -2 -2 -2 -2 60 -2 -2 -2 67 -2 -2 -2 65 -2 -2 -2
   69 -2 -2 -2 -2 -2 -2 -2 71 -2 -2 -2 65 -2 -2 -2
   62 -2 -2 -2 64 -2 -2 -2 71 -2 -2 -2 65 -2 -2 -2])

(deftest valid?-test
  (is (not (valid? invalid))))
