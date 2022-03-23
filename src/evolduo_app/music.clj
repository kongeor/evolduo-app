(ns evolduo-app.music
  (:require [clojure.string :as string]))

(def measure-sixteens 16)

(def major-intervals [0 2 4 5 7 9 11])
(def minor-intervals [0 2 3 5 7 8 10])

;; not useful
(defn intervals* [intervals]
  (apply concat
    (map (fn [i]
           (map (partial + (* i 12)) intervals)) (iterate inc 0))))

(defn ->abc-key [key mode]
  (if (= mode :minor)
    (str key "m")
    key))

(comment
  (nth (intervals* major-intervals) 100))

(comment
  ((intervals* major-intervals) 100))                       ;; don't eval this

(def abc-note-map
  {-1 "z"
   48 "C,"
   49 "^C,"
   50 "D,"
   51 "^D,"
   52 "E,"
   53 "F,"
   54 "^F,"
   55 "G,"
   56 "^G,"
   57 "A,"
   58 "^A,"
   59 "B,"
   60 "C"
   61 "^C"
   62 "D"
   63 "^D"
   64 "E"
   65 "F"
   66 "^F"
   67 "G"
   68 "^G"
   69 "A"
   70 "^A"
   71 "B"
   72 "c"
   73 "^c"
   74 "d"
   75 "^d"
   76 "e"
   77 "f"
   78 "^f"
   79 "g"
   80 "^g"
   81 "a"
   82 "^a"
   83 "b"
   })

(def note-abc-map (clojure.set/map-invert abc-note-map))

(defn abc-note-dur [cnt]
  (let [t (/ cnt 4)]
    (condp = t
      1/4 "/2"
      1/2 ""
      ; 3/4 ""                                                ;; fix?
      1 "2"
      ; 5/4 ""                                                ;; fix?
      3/2 "3"
      ; 7/4 ""                                                ;; fix?
      2 "4"
      4 "8")))

(def c [60 -2 -2 -2 -2 -2 -2 -2 60 -2 -2 -2 -2 -2 -2 -2
        62 64 67 -2 -1 -2 -2 -2 67 -2 69 -2 -2 -2 -2 -2
        -1 -2 -2 -2 59 -2 60 -2 62 -2 64 -2 65 -2 -2 -2
        64 -2 -2 -2 64 -2 -2 -2 64 64 -1 -2 -2 -2 -2 -2
        ])

(comment
  (reduce (fn [acc n]
            (if (= n -2)
              (update-in acc [(dec (count acc)) :total] inc)
              (conj acc {:note n :total 1})
              ))
    []
    c))

(comment
  (update-in [{:note 60 :count 1}] [0 :count] inc))

(defn calc-note-times [notes]
  (reduce (fn [acc n]
            (if (= n -2)
              (update-in acc [(dec (count acc)) :total] inc)
              (conj acc {:note n :total 1})
              ))
    []
    notes))

(defn note->abc [{:keys [note total]}]
  (str (abc-note-map note) (abc-note-dur total)))

(defn chromo->measures [chromo]
  (partition measure-sixteens chromo))

(defn measure->abc [measure]
  (->> measure
    calc-note-times
    (map note->abc)
    (clojure.string/join " ")))

(defn chromo->abc [chromo]
  (clojure.string/join " | " (map measure->abc (chromo->measures chromo))))

(def c1 [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
        ])

(defn calc-note-length [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (inc (count (take-while #(= % -2) (drop (inc note-idx) c)))))

(defn split-note [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (let [l (calc-note-length c note-idx)]
    (assoc c (+ (/ l 2)) (c note-idx))))

(defn next-note-idx [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (+ note-idx (inc (count (take-while #(= % -2) (drop (inc note-idx) c))))))

(comment
  (next-note-idx c1 0))

(defn merge-note [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (let [l1 (calc-note-length c note-idx)
        idx2 (next-note-idx c note-idx)
        l2 (calc-note-length c idx2)]
    (when (= l1 l2)
      (assoc c idx2 -2))))

(comment
  (take 16 (-> c1
             (split-note 0)
             (split-note 0)
             (split-note 0)
             (split-note 0)
             (merge-note 0)
             (merge-note 0)
             (merge-note 0)
             (merge-note 0)
             )))


(comment
  (take 16 (-> c1
             (split-note 0)
             (split-note 0)
             (split-note 0)
             (split-note 0))))

(comment
  (calc-note-length c1 1))

(comment
  (chromo->abc c1))

(comment
  (clojure.string/join " | " (map measure->abc (chromo->measures c))))

(comment
  (calc-note-times (first (chromo->measures c))))

(comment
  (->> c
    (reduce
      (fn [acc x]
        (if (= x -2)
          (conj acc (last acc))
          (conj acc x)))
      []
      )
    (partition-by identity)
    #_(map (fn [g]
           {:note  (first g)
            :total (count g)}))
    #_(map (fn [{:keys [note total]}]
           (str (abc-note-map note) (abc-note-dur total))))
    ;; measures
    #_(clojure.string/join " ")
    ))

(defn ->abc [{:keys [id key genes] :as data}]
  (let [abc-genes (chromo->abc genes)]
    (str
      "X:1\n"
      "T:" id "\n"
      "K:" key "\n"
      abc-genes)))

(comment
  (->abc {:id 42
          :key "C#"
          :genes c1}))


(defn key->int-note [k]
  (let [[n s] k
        abc-note (if s (str "^" n) (str n))]
    (println n s abc-note)
    (note-abc-map abc-note)))

(comment
  #_(note-abc-map "C")
  (key->int-note "C"))

(defn mode->nums [m]
  (case m
    :major major-intervals
    :minor minor-intervals))

(defn gen-track [{:keys [key measures mode]}]
  (let [root-note (key->int-note key)
        scale-notes (mode->nums mode)]
    (take (* 16 measures) (mapcat (fn [iv] [(+ root-note iv) -2 -2 -2]) (cycle scale-notes)))))

(comment
  (let [key "C"]
    (->abc {:id "foo" :key key :genes (gen-track {:key key :measures 4 :mode :minor})})))

;; chords

(defn gen-chord [{:keys [key mode duration degree]}]
  (let [root-note (key->int-note key)
        scale-notes (intervals* (mode->nums mode))
        chord-notes (map #(+ root-note (nth scale-notes (+ degree %))) [0 2 4 6])]
    (println "chord notes" degree chord-notes)
    (str "[" (string/join (map str (map abc-note-map chord-notes) (repeat duration))) "]")
    ))

(comment
  (gen-chord {:key "C" :mode :major :duration 8 :degree 0}))

(def degrees {"I" 0
              "II" 1
              "III" 2
              "IV" 3
              "V" 4})

(defn pattern->degrees [mode pattern]
  (map degrees (string/split pattern #"-")))

(comment
  (pattern->offsets :major "I-IV-V-I"))

(defn gen-chord-progression [{:keys [key mode duration pattern]}]
  (let [dgs (pattern->degrees mode pattern)
        chords (map #(gen-chord {:key key :mode mode :duration duration :degree %}) dgs)]
    (string/join " | " chords)))


(comment
  (gen-chord-progression {:key "C" :mode :major :duration 8 :pattern "I-IV-V-I"}))

(defn progression->abc [{:keys [key mode pattern] :as data}]
  (str
    "X:1\\n"
    "K:" (->abc-key key mode) "\\n"
    (str "| " (gen-chord-progression {:key key :mode mode :duration 8 :pattern pattern})
      "|")))

(comment
  (progression->abc {:key "C" :pattern "I-IV"}))

#_(mode->nums :major)