(ns evolduo-app.music-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music :refer [chromo->abc]]))

(def c1 [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         ])

(deftest chromo->abc-test
  (is (= "\\\"A\\\" C8 | \\\"B\\\" D8 | \\\"C\\\" E8 | \\\"D\\\" F8" (chromo->abc c1 ["A" "B" "C" "D"]))))
