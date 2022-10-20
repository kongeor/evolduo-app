(ns evolduo-app.music.stats
  (:require [chickn.math :as cmath]))

(defn compute-iteration-stats [chromos]
  (let [xs (map :fitness chromos)
        min (apply min xs)
        max (apply max xs)
        mean (cmath/mean xs)
        std-dev (cmath/std-dev xs)]
    {:min min
     :max max
     :mean mean
     :std-dev std-dev}))

