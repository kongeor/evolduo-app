(ns evolduo-app.music
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(def measure-sixteens 16)

(def music-keys ["C" "Db" "D" "Eb" "E" "F" "F#" "G" "Ab" "A" "Bb" "B"])

(def ionian-intervals [2 2 1 2 2 2 1])

(defn intervals->scale [ivs]
  (reduce (fn [acc i] (conj acc (+ i (last acc)))) [0] ivs))

(comment
  (intervals->scale ionian-intervals))

(defn rotate-intervals [ivs degree]
  (take 6 (drop degree (cycle ivs))))

(comment
  (rotate-intervals ionian-intervals 5))

(defn scale-notes [intervals degree]
  (intervals->scale (rotate-intervals intervals degree)))

(comment
  (scale-notes ionian-intervals 1))

(def modes [["major"      (scale-notes ionian-intervals 0)]
            ["dorian"     (scale-notes ionian-intervals 1)]
            ["phrygian"   (scale-notes ionian-intervals 2)]
            ["lydian"     (scale-notes ionian-intervals 3)]
            ["mixolydian" (scale-notes ionian-intervals 4)]
            ["minor"      (scale-notes ionian-intervals 5)]
            ["locrian"    (scale-notes ionian-intervals 6)]])

(def mode-map (into {} modes))

(def mode-names (mapv first modes))


(defn mode-degree [mode]
  (.indexOf mode-names mode))

(comment
  (mode-degree "locrian"))

(defn transpose-key [key mode]
  (let [d (mode-degree mode)
        ki (.indexOf music-keys key)
        df (apply + (take d ionian-intervals))
        idx (mod (- (+ ki 24) df) 12)]
    (nth music-keys idx)))

(comment
  (transpose-key "C" "lydian"))

;; progressions

(def all-degrees-progression "I-II-III-IV-V-VI-VII-I")

(def progressions ["I-IV-V-I"
                   "I-II-VI-I"
                   "I-IV-I-IV"
                   "I-I-VII-I"
                   all-degrees-progression
                   ])

(def chord-intervals [["R" [0]]
                      ["R + 5 + R" [0 4 7]]
                      ["R + 3 + 3" [0 2 4]]
                      ["R + 3 + 3 + 3" [0 2 4 6]]])

(def chord-intervals-keys (mapv first chord-intervals))
(def chord-intervals-map (reduce conj {} chord-intervals))

;; not useful
(defn intervals* [intervals]
  (apply concat
    (map (fn [i]
           (map (partial + (* i 12)) intervals)) (iterate inc 0))))

(defn minor-mode? [[_ _ third]]
  (condp = third
    3 true
    4 false))

(comment
  (minor-mode? (get mode-map "dorian")))

(defn mode->scale [m]
  (let [s (get mode-map m)]
    (assert s (str "Unknown scale: " m))
    s))

(defn ->abc-key [key mode-name]
  ;; TODO fix?
  (str key " " (str/capitalize mode-name))
  #_(if (minor-mode? (mode->scale mode-name))
    (str key "m")
    key))

(def notes      ["C" "C#" "D" "D#" "E" "F" "F#" "G" "G#" "A" "A#" "B"])
(def notes-flat ["C" "Db" "D" "Eb" "E" "F" "Gb" "G" "Ab" "A" "Bb" "B"])

(def sharps
  {"C"  #{}
   "G"  #{6}
   "D"  #{6 1}
   "A"  #{6 1 8}
   "E"  #{6 1 8 3}
   "B"  #{6 1 8 3 10}
   "F#" #{6 1 8 3 10 5}})

(def flats
  {"F"  #{10}
   "Bb" #{10 3}
   "Eb" #{10 3 8}
   "Ab" #{10 3 8 1}
   "Db" #{10 3 8 1 6}})

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

(def note-abc-map (set/map-invert abc-note-map))

(def abc-note-map-flats
  {-1 "z"
   48 "C,"
   49 "_D,"
   50 "D,"
   51 "_E,"
   52 "E,"
   53 "F,"
   54 "_G,"
   55 "G,"
   56 "_A,"
   57 "A,"
   58 "_B,"
   59 "B,"
   60 "C"
   61 "_D"
   62 "D"
   63 "_E"
   64 "E"
   65 "F"
   66 "_G"
   67 "G"
   68 "_A"
   69 "A"
   70 "_B"
   71 "B"
   72 "c"
   73 "_d"
   74 "d"
   75 "_e"
   76 "e"
   77 "f"
   78 "_g"
   79 "g"
   80 "_a"
   81 "a"
   82 "_b"
   83 "b"
   })

(def note-abc-map-flats (set/map-invert abc-note-map-flats))

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

(defn chromatic-chromosome [root chords]
  (take (* (count chords) measure-sixteens)
    (->> (iterate inc root)
      (map #(cons % [-2 -2 -2]))
      (apply concat))))

(comment
  (reduce (fn [acc n]
            (if (= n -2)
              (update-in acc [(dec (count acc)) :duration] inc)
              (conj acc {:note n :duration 1})
              ))
    []
    c))

(comment
  (update-in [{:note 60 :count 1}] [0 :count] inc))

(defn calc-note-times [measure]
  (reduce (fn [acc n]
            (if (= n -2)
              (update-in acc [(dec (count acc)) :duration] inc)
              (conj acc {:note n :duration 1})
              ))
    []
    measure))

(defn note->abc-sharps [{:keys [note duration key]}]
  (let [sharp-notes (get sharps key {})
        natural-notes (->> sharp-notes (map dec) (into #{})) ;; TODO memo
        oct-note (mod note 12)
        sharp? (sharp-notes oct-note)
        natural? (natural-notes oct-note)
        abc-note (abc-note-map note)
        abc-note' (cond
                    ; (and sharp? natural?) (str "^" (abc-note-map (dec note)))
                    ; sharp? (if (= 2 (count abc-note))
                    ;          (subs abc-note 1)
                    ;          abc-note)
                    natural? (str "=" abc-note)
                    :else abc-note)]
    (str abc-note' (abc-note-dur duration))))

(defn note->abc-flats [{:keys [note duration key]}]
  (let [flat-notes (get flats key {})
        natural-notes (->> flat-notes (map inc) (into #{})) ;; TODO memo
        oct-note (mod note 12)
        flat? (flat-notes oct-note)
        natural? (natural-notes oct-note)
        abc-note (abc-note-map-flats note)
        abc-note' (cond
                    ; (and flat? natural?) (str "_" (abc-note-map-flats (inc note)))
                    ; flat? (if (= 2 (count abc-note))
                    ;          (subs abc-note 1)
                    ;          abc-note)
                    natural? (str "=" abc-note)
                    :else abc-note)]
    (str abc-note' (abc-note-dur duration))))

(defn sharp? [key]
  ((set (keys sharps)) key))                                ;; TODO memo

(comment
  (sharp? "Bb"))

(defn note->abc [{:keys [note duration key mode] :as data}]
  (let [k (transpose-key key mode)
        d {:note note :duration duration :key k}]
    (if (or (nil? key) (sharp? k))
      (note->abc-sharps d)
      (note->abc-flats d))))

(defn chromo->measures [chromo]
  (partition measure-sixteens chromo))

(defn measure->abc [measure key mode]
  (->> measure
    calc-note-times
    (map #(assoc % :key key :mode mode))
    (map note->abc)
    (str/join " ")))

(defn chromo->abc [chromo chord-names key mode]
  (let [measures (chromo->measures chromo)]
    (str/join " | " (map #(str "\\\"" %2 "\\\" " (measure->abc % key mode)) measures chord-names))))

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

(defn note? [n]
  (when (and
          (not= n -1)
          (not= n -2))
    n))

(defn chromo->measure-notes [c]
  (map (fn [m] (filter note? m)) (partition measure-sixteens c)))

(comment
  (chromo->measure-notes c1))

(comment
  (take 16 c1)
  (take 16 (-> c1
             (split-note 0)
             (split-note 0))))

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
  (chromo->measures c)
  (calc-note-times (second (chromo->measures c))))

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
  (let [[n acc] k]
    (if (sharp? k)
      (let [abc-note (if acc (str "^" n) (str n))]
        (note-abc-map abc-note))
      (let [abc-note (if acc (str "_" n) (str n))]
        (note-abc-map-flats abc-note)))))

(comment
  (key->int-note "G"))

(defn gen-track [{:keys [key measures mode]}]
  (let [root-note (key->int-note key)
        scale-notes (mode->scale mode)]
    (take (* 16 measures) (mapcat (fn [iv] [(+ root-note iv) -2 -2 -2]) (cycle scale-notes)))))

(defn random-track [{:keys [key measures mode]}]
  (let [root-note (key->int-note key)
        scale-notes (mode->scale mode)
        ;; one day this will be much more intelligent
        scale-notes* (apply concat (repeat 10 scale-notes))]
    (take (* 16 measures) (mapcat (fn [iv] [(+ root-note iv) -2 -2 -2]) (shuffle scale-notes*)))))

(comment
  (random-track {:key "C" :measures 4 :mode "major"}))

(comment
  (let [key "C"]
    (->abc {:id "foo" :key key :genes (gen-track {:key key :measures 4 :mode "minor"})})))

;; chords

(defn chord-intervals [[r & rs]]
  (reduce #(conj % (- %2 r)) #{0} rs))

(def chords
  {#{0 4 7}    ""
   #{0 3 7}    "m"
   #{0 3 6}    "dim"
   #{0 4 7 10} "7"
   #{0 3 7 10} "m7"
   #{0 4 7 11} "Maj7"
   #{0 3 7 11} "m(Maj7)"
   #{0 3 6 10} "dim7"
   #{0 3 6 11} "dim(Maj7)"
   })

(defn chord->str [[root :as chord] key]
  (let [notes (if (sharp? key) notes notes-flat)
        r (get notes (mod root 12))
        chord-ivs (chord-intervals chord)
        chord-str (chords chord-ivs)]
    (str r chord-str)))

(comment
  (chord->str [60 63 67])
  )

(defn gen-chord [{:keys [key mode duration degree chord]}]
  (let [root-note (key->int-note key)
        scale-notes (intervals* (mode->scale mode))
        chord-notes (map #(+ root-note (nth scale-notes (+ degree %))) (get chord-intervals-map chord [0 2 4]))
        chord-str (chord->str chord-notes key)]
    (str "\\\"" chord-str "\\\" [ "  (str/join (map str (map abc-note-map chord-notes) (repeat duration))) "]")
    ))

(defn gen-chord-notes [{:keys [key mode duration degree chord]}]
  (let [root-note (key->int-note key)
        scale-notes (intervals* (mode->scale mode))
        chord-notes (map #(+ root-note (nth scale-notes (+ degree %))) (get chord-intervals-map chord [0 2 4]))]
    chord-notes))

(defn gen-chord-2 [{:keys [key mode duration degree chord] :as params}] ;; yey and also, check duration
  (let [chord-notes (gen-chord-notes params)]
    (chord->str chord-notes key)))

(comment
  (gen-chord-notes {:key "C" :mode "major" :duration 8 :degree 0 :chord "R + 3 + 3 + 3"})
  (gen-chord-2 {:key "C" :mode "major" :duration 8 :degree 0 :chord "R + 3 + 3 + 3"}))

(def degrees {"I" 0
              "II" 1
              "III" 2
              "IV" 3
              "V" 4
              "VI" 5
              "VII" 6})

(defn progression->degrees [mode progression]
  (map degrees (str/split progression #"-")))

(comment
  (progression->degrees :major "I-IV-V-I"))

(defn gen-chord-progression [{:keys [key mode duration progression chord]}]
  (let [dgs (progression->degrees mode progression)
        chords (map #(gen-chord {:key key :mode mode :duration duration
                                 :chord chord :degree %}) dgs)]
    (str/join " | " chords)))

(defn gen-chord-progression-notes [{:keys [key mode duration progression chord]}]
  (let [dgs (progression->degrees mode progression)
        chords (map #(gen-chord-notes {:key key :mode mode :duration duration
                                       :chord chord :degree %}) dgs)]
    chords))

(comment
  (gen-chord-progression-notes {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I"}))

(defn gen-chord-names [{:keys [key mode duration progression chord] :as settings}]
  (let [dgs (progression->degrees mode progression)]
    (map #(gen-chord-2 (merge settings {:degree %})) dgs)))

(comment
  (gen-chord-names {:key "C" :mode "major" :progression all-degrees-progression :chord "R + 3 + 3 + 3"}))

(comment
  (gen-chord-progression {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I"}))

(defn progression->abc [{:keys [key mode progression] :as data}]
  (str
    "X:1\\n"
    "K:" (->abc-key key mode) "\\n"
    (str "| " (gen-chord-progression {:key key :mode mode :duration 8 :progression progression})
      "|")))

(comment
  (progression->abc {:key "C" :mode "major" :progression "I-IV"}))

(comment
  (->abc-key "C" :dorian)
  )

(defn ->abc-track
  [{:keys [key mode progression chord tempo] :as settings} {:keys [genes]}]
  (let [chord-names (gen-chord-names settings)]
    (str
      "X:1\\n"
      "K:" (->abc-key key mode) "\\n"
      "Q:" tempo "\\n"
      "V:V1 clef=treble \\n"
      ; "V:V2 clef=bass \\n"
      (str "[V:V1] | " (chromo->abc genes chord-names key mode) " | \\n")
      #_(str "[V:V2] | " (gen-chord-progression {:key key :mode mode
                                                 :chord chord
                                                 :duration 8 :progression progression})
          "|"))))

(comment
  (->abc-track {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :chord "R + 3 + 3 + 3"} {:genes c}))


(comment
  (->abc-track {:key "A" :mode "major" :duration 8 :progression "I-IV-V-I" :chord "R + 3 + 3 + 3"} {:genes (chromatic-chromosome 60)}))
