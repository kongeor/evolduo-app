(ns evolduo-app.music.fitness
  (:require [evolduo-app.music :as muse]))

(defn oct-note [note]
  (when (not= note -1)
    (mod note 12)))

(comment
  (oct-note 71))

(defn analyze [settings {:keys [genes] :as chromo}]
  (let [measure-notes (map muse/calc-note-times (muse/chromo->measures genes))
        chord-progression (muse/gen-chord-progression-notes settings)
        measures (muse/chromo->measures genes)]
    (assert (= (count chord-progression)
              (count measures)) "Measure count does not match progression chord count")
    (let [analyzed-measures (map (fn [m c]
                                   (map (fn [n]
                                          (let [n' (oct-note (:note n))
                                                c' (set (map oct-note c))] ;; memo
                                            (assoc n :chord (set c)
                                                     :oct-note n'
                                                     :oct-chord c'
                                                     :type (if n' :note :rest)))) m)) measure-notes chord-progression)]
      (apply concat analyzed-measures))
    ))

(comment
  (analyze {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I"} {:genes muse/c1}))

(defn fitness [settings chromo]
  (let [a (analyze settings chromo)]
    (reduce (fn [acc {:keys [duration type oct-chord oct-note]}]
              (if (and (= type :note)
                    (not (oct-chord oct-note)))
                (- acc duration)
                acc)) 0 a)))

(comment
  (fitness {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I"} {:genes muse/c1}))