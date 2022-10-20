(ns evolduo-app.model.iteration-test
  (:require [clojure.test :refer :all]
            [evolduo-app.music.stats :as stats]
            [evolduo-app.model.iteration :refer [adjust-iteration-chromosome-fitness]]))


(def chromos1 [{:id 1 :fitness -20}
               {:id 2 :fitness 20}
               {:id 3 :fitness 60}
               {:id 4 :fitness 80}])

(def ratings1 {1 -2
               2 2
               3 -1
               4 1})

(def stats1 (stats/compute-iteration-stats chromos1))

(def chromos2 [{:id 1 :fitness -2}
               {:id 2 :fitness 2}
               {:id 3 :fitness 6}
               {:id 4 :fitness 8}])

(def stats2 (stats/compute-iteration-stats chromos2))

(def chromos3 [{:id 1 :fitness -2}
               {:id 2 :fitness 2}
               {:id 3 :fitness 160}
               {:id 4 :fitness 280}])

(def stats3 (stats/compute-iteration-stats chromos3))

(deftest adjust-iteration-chromosome-fitness-test
  (testing "just ratings based test - dataset 1"
    (is (= [{:fitness     -28
             :id          1
             :raw_fitness -20}
            {:fitness     28
             :id          2
             :raw_fitness 20}
            {:fitness     48
             :id          3
             :raw_fitness 60}
            {:fitness     96
             :id          4
             :raw_fitness 80}]
         (adjust-iteration-chromosome-fitness ratings1 nil chromos1))))
  (testing "stats based test - dataset 1"
    (is (= [{:fitness     -70
             :id          1
             :raw_fitness -20}
            {:fitness     70
             :id          2
             :raw_fitness 20}
            {:fitness     35
             :id          3
             :raw_fitness 60}
            {:fitness     105
             :id          4
             :raw_fitness 80}]
         (adjust-iteration-chromosome-fitness ratings1 stats1 chromos1))))
  (testing "just ratings based test - dataset 2"
    (is (= [{:fitness     -3
             :id          1
             :raw_fitness -2}
            {:fitness     3
             :id          2
             :raw_fitness 2}
            {:fitness     5
             :id          3
             :raw_fitness 6}
            {:fitness     10
             :id          4
             :raw_fitness 8}]
         (adjust-iteration-chromosome-fitness ratings1 nil chromos2))))
  (testing "stats based test - dataset 2"
    (is (= [{:fitness     -22
             :id          1
             :raw_fitness -2}
            {:fitness     22
             :id          2
             :raw_fitness 2}
            {:fitness     -4
             :id          3
             :raw_fitness 6}
            {:fitness     18
             :id          4
             :raw_fitness 8}]
         (adjust-iteration-chromosome-fitness ratings1 stats2 chromos2))))
  (testing "just ratings based test - dataset 3"
    (is (= [{:fitness     -3
             :id          1
             :raw_fitness -2}
            {:fitness     3
             :id          2
             :raw_fitness 2}
            {:fitness     128
             :id          3
             :raw_fitness 160}
            {:fitness     336
             :id          4
             :raw_fitness 280}]
           (adjust-iteration-chromosome-fitness ratings1 nil chromos3))))
  (testing "stats based test - dataset 3"
    (is (= [{:fitness     -143
             :id          1
             :raw_fitness -2}
            {:fitness     143
             :id          2
             :raw_fitness 2}
            {:fitness     90
             :id          3
             :raw_fitness 160}
            {:fitness     351
             :id          4
             :raw_fitness 280}]
           (adjust-iteration-chromosome-fitness ratings1 stats3 chromos3))))
  )
