(ns evolduo-app.rand
  (:require [clojure.string :as str]))

(def digits (take 10 (iterate inc 0)))

(defn random-num [no-of-digits]
  (str/join (repeatedly no-of-digits #(rand-nth digits))))

(comment
  (random-num 6))
