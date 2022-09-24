(ns evolduo-app.music.fitness
  (:require [evolduo-app.music :as muse]))

(defn oct-note [note]
  (when (not= note -1)
    (mod note 12)))

(comment
  (oct-note 71))

(defn analyze [settings genes]
  (let [measure-notes     (map muse/calc-note-times (muse/chromo->measures genes))
        chord-progression (muse/gen-chord-progression-notes settings)
        measures          (muse/chromo->measures genes)]
    (assert (= (count chord-progression)
               (count measures)) "Measure count does not match progression chord count")
    (let [analyzed-measures (map (fn [m c]
                                   (map-indexed (fn [i n]
                                                  (let [n' (oct-note (:note n))
                                                        c' (set (map oct-note c))] ;; memo
                                                    (assoc n :chord (set c)
                                                             :oct-note n'
                                                             :oct-chord c'
                                                             :type (if n' :note :rest)
                                                             :measure-last-note? (= (count m) (inc i))))) m)) measure-notes chord-progression)]
      (apply concat analyzed-measures))
    ))

;; there are no rests right now,
;; when added, the :measure-last-note? will need to be computed differently

(defn calc-last-note-score [{:keys [duration oct-note oct-chord type]}]
  (if (and (= type :note)
           (oct-chord oct-note))
    (* duration 5 (condp = oct-note
                    0 1                                     ;; root
                    3 0.7                                   ;; minor 3rd
                    4 0.7                                   ;; major 3rd
                    7 0.5                                   ;; 5th
                    10 -1                                   ;; minor 7th
                    11 -1                                   ;; major 7th
                    0
                    ))
    0))

(comment
  (calc-last-note-score
    {:note               65,
     :duration           4,
     :index              0,
     :chord              #{60 64 67},
     :oct-note           10,
     :oct-chord          #{0 7 4 10},
     :type               :note,
     :measure-last-note? true}))


(defn calc-last-notes-score [notes]
  (->> notes
       (filter :measure-last-note?)
       (map calc-last-note-score)
       (apply +)))

(comment
  (analyze {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1} muse/c1)
  (calc-last-notes-score (analyze {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1} muse/c)))

(defn fitness [settings genes]
  (let [a (analyze settings genes)]
    (+
      (calc-last-notes-score a)
      (reduce (fn [acc {:keys [duration type oct-chord oct-note]}]
                (if (and (= type :note)
                         (not (oct-chord oct-note)))
                  (- acc duration)
                  acc)) 0 a))))

(comment
  (fitness {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1} muse/c1))


(defn valid? [genes]
  (let [measures (muse/chromo->measures genes)]
    (->> measures
         (map first)
         (filter #(= -2 %))
         seq
         not)))

(defn maybe-fix
  "Simplistic fix for invalid chromosomes. Just add the root pitch for
   measures that start with a prolongation (-2)."
  [{:keys [key]} genes]
  (if (valid? genes)
    genes
    (let [note     (muse/key->int-note key)
          measures (muse/chromo->measures genes)]
      (vec (apply concat
                  (map #(if (= (first %) -2)
                          (assoc (vec %) 0 note)
                          %) measures))))))
