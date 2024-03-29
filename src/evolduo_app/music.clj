(ns evolduo-app.music
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(def measure-sixteens 16)

(def music-keys ["C" "Db" "D" "Eb" "E" "F" "F#" "G" "Ab" "A" "Bb" "B"])
(def music-keys-restricted ["C" "D"])

(def notes      ["C" "C#" "D" "D#" "E" "F" "F#" "G" "G#" "A" "A#" "B"])
(def notes-flat ["C" "Db" "D" "Eb" "E" "F" "Gb" "G" "Ab" "A" "Bb" "B"])

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

(defn transpose-key' [key mode]
  (let [d (mode-degree mode)
        ki (.indexOf music-keys key)
        df (apply + (take d ionian-intervals))
        idx (mod (- (+ ki 24) df) 12)]
    (nth music-keys idx)))

;; based on https://github.com/paulrosen/abcjs/blob/main/src/const/relative-major.js

(def relative-majors {
                      "C"  ["CMaj", "Amin", "Am", "GMix", "DDor", "EPhr", "FLyd", "BLoc"],
                      "Db" ["DbMaj", "Bbmin", "Bbm", "AbMix", "EbDor", "FPhr", "GbLyd", "CLoc"],
                      "D"  ["DMaj", "Bmin", "Bm", "AMix", "EDor", "F#Phr", "GLyd", "C#Loc"],
                      "Eb" ["EbMaj", "Cmin", "Cm", "BbMix", "FDor", "GPhr", "AbLyd", "DLoc"],
                      "E"  ["EMaj", "C#min", "C#m", "BMix", "F#Dor", "G#Phr", "ALyd", "D#Loc"],
                      "F"  ["FMaj", "Dmin", "Dm", "CMix", "GDor", "APhr", "BbLyd", "ELoc"],
                      "Gb" ["GbMaj", "Ebmin", "Ebm", "DbMix", "AbDor", "BbPhr", "CbLyd", "FLoc"],
                      "G"  ["GMaj", "Emin", "Em", "DMix", "ADor", "BPhr", "CLyd", "F#Loc"],
                      "Ab" ["AbMaj", "Fmin", "Fm", "EbMix", "BbDor", "CPhr", "DbLyd", "GLoc"],
                      "A"  ["AMaj", "F#min", "F#m", "EMix", "BDor", "C#Phr", "DLyd", "G#Loc"],
                      "Bb" ["BbMaj", "Gmin", "Gm", "FMix", "CDor", "DPhr", "EbLyd", "ALoc"],
                      "B"  ["BMaj", "G#min", "G#m", "F#Mix", "C#Dor", "D#Phr", "ELyd", "A#Loc"],
                      ;;    Enharmonic keys
                      "C#" ["C#Maj", "A#min", "A#m", "G#Mix", "D#Dor", "E#Phr", "F#Lyd", "B#Loc"],
                      "F#" ["F#Maj", "D#min", "D#m", "C#Mix", "G#Dor", "A#Phr", "BLyd", "E#Loc"],
                      "Cb" ["CbMaj", "Abmin", "Abm", "GbMix", "DbDor", "EbPhr", "FbLyd", "BbLoc"],
                      })

(def relative-major-map
  (reduce-kv (fn [m k v]
               (reduce (fn [a n]
                         (assoc a n k)) m v)
               ) {} relative-majors))

;; I'm too lazy to rewrite this so will just write some code to convert it

(def mode-name-mapping
  {"minor" "m"
   "major" "Maj"
   "mixolydian" "Mix"
   "dorian" "Dor"
   "phrygian" "Phr"
   "lydian" "Lyd"
   "locrian" "Loc"
   })

(defn transpose-key [key mode]
  (let [mapped-mode (get mode-name-mapping mode)]
    (assert mapped-mode (str "unknown mode for " key " " mode))
    (if-let [relative-major (get relative-major-map (str key mapped-mode))]
      relative-major
      (let [[k' _] key]
        (get relative-major-map (str k' mapped-mode) key)))))

(comment
  (get relative-major-map "ELoc")
  (transpose-key "Eb" "locrian")
  (transpose-key "C" "locrian")
  (transpose-key "F#" "mixolydian"))

;; progressions

(def all-degrees-progression "I-II-III-IV-V-VI-VII-I")

(def progressions ["I-I-I-I"
                   "I-IV-V-I"
                   "I-II-VI-I"
                   "I-IV-I-IV"
                   "I-I-VII-I"
                   "II-V-I-I"

                   "VI-II-V-I"                              ;;  Circle
                   "I-IV-II-V"                              ;; Montgomery-Ward bridge
                   "V-III-V-III"                                  ;; Irregular
                   "I-V-VI-III-IV-I-IV-V"                   ;; Pachelbel's Canon
                   "I-I-I-I-IV-IV-I-I-V-V-I-I"
                   ;; all-degrees-progression
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
  (get mode-map "dorian")
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
   "Db" #{10 3 8 1 6}
   "Gb" #{10 3 8 1 6 11}
   "B"  #{10 3 8 1 6 11 4}})

(def abc-note-map
  {-1 "z"
   36 "C,,"
   37 "^C,,"
   38 "D,,"
   39 "^D,,"
   40 "E,,"
   41 "F,,"
   42 "^F,,"
   43 "G,,"
   44 "^G,,"
   45 "A,,"
   46 "^A,,"
   47 "B,,"
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
   84 "c'"
   85 "^c'"
   86 "d'"
   87 "^d'"
   88 "e'"
   89 "f'"
   90 "^f'"
   91 "g'"
   92 "^g'"
   93 "a'"
   94 "^a'"
   95 "b'"
   })

(def note-abc-map (set/map-invert abc-note-map))

(def abc-note-map-flats
  {-1 "z"
   36 "C,,"
   37 "_D,,"
   38 "D,,"
   39 "_E,,"
   40 "E,,"
   41 "F,,"
   42 "_G,,"
   43 "G,,"
   44 "_A,,"
   45 "A,,"
   46 "_B,,"
   47 "B,,"
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
   84 "c'"
   85 "_d'"
   86 "d'"
   87 "_e'"
   88 "e'"
   89 "f'"
   90 "_g'"
   91 "g'"
   92 "_a'"
   93 "a'"
   94 "_b'"
   95 "b'"
   })

(def note-abc-map-flats (set/map-invert abc-note-map-flats))

(defn abc-note-dur [cnt]
  (let [t (/ cnt 4)]
    (condp = t
      1/4 "/2"
      1/2 ""
      3/4 ""                                                ;; fix?
      1 "2"
      5/4 "2"                                                ;; fix?
      3/2 "3"
      7/4 "3"                                                ;; fix?
      2 "4"
      5/2 ""                                                ;; fix?
      3 "6"
      4 "8"
      ""                                                    ;; 7/2 etc. TODO cleanup
      )))

(def c [60 -2 -2 -2 -2 -2 -2 -2 60 -2 -2 -2 -2 -2 -2 -2
        62 64 67 -2 -1 -2 -2 -2 67 -2 69 -2 -2 -2 -2 -2
        -1 -2 -2 -2 59 -2 60 -2 62 -2 64 -2 65 -2 -2 -2
        64 -2 -2 -2 64 -2 -2 -2 64 64 -1 -2 -2 -2 -2 -2
        ])

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

(defn calc-note-times- [measure]
  (reduce (fn [acc n]
            (if (= n -2)
              (update-in acc [(dec (count acc)) :duration] inc)
              (conj acc {:note n :duration 1})
              ))
    {:notes [] :index 0}
    measure))

(defn calc-note-times [measure]
  (loop [i 0
         notes []]
    (if (>= i (count measure))
      notes
      (recur
        (inc i)
        (let [note (nth measure i)]
          (if (= -2 note)
            (update-in notes [(dec (count notes)) :duration] inc)
            (conj notes {:note note :duration 1 :index i})
            ))))))

(defn note->abc-sharps [{:keys [note duration key]} sharp-notes]
  (let [oct-note (mod note 12)
        next-oct-note (mod (inc oct-note) 12)
        sharp-sig? (sharp-notes oct-note)
        abc-note (abc-note-map note)
        sharp?    (str/starts-with? abc-note "^")
        abc-note'     (cond
                        (and sharp? sharp-sig?) [(subs abc-note 1) sharp-notes]
                        (sharp-notes next-oct-note) [(str "=" abc-note) (disj sharp-notes next-oct-note)]
                        sharp? [abc-note (conj sharp-notes oct-note)]
                        :else [abc-note sharp-notes])]
    [(str (first abc-note') (abc-note-dur duration)) (second abc-note')]))

(defn note->abc-flats [{:keys [note duration key]} flat-notes]
  (let [oct-note (mod note 12)
        prev-oct-note (mod (dec oct-note) 12)
        flat-sig? (flat-notes oct-note)
        abc-note (abc-note-map-flats note)
        flat?    (str/starts-with? abc-note "_")
        abc-note'     (cond
                        (and flat? flat-sig?) [(subs abc-note 1) flat-notes]
                        (flat-notes prev-oct-note) [(str "=" abc-note) (disj flat-notes prev-oct-note)]
                        flat? [abc-note (conj flat-notes oct-note)]
                        :else [abc-note flat-notes])]
    [(str (first abc-note') (abc-note-dur duration)) (second abc-note')]))

(defn sharp? [key]
  ((set (keys sharps)) key))                                ;; TODO memo

(defn flat? [key]
  ((set (keys flats)) key))                                ;; TODO memo

(comment
  (get sharps "G")
  (sharp? "G")
  (sharp? "Bb"))

(defn note->abc [{:keys [sharp?] :as data} & {:keys [sharps flats] :or {sharps #{} flats #{}}}]
  (if sharp?
    (note->abc-sharps data sharps)
    (note->abc-flats data flats)))

(defn measure->abc [measure key mode]
  (let [key'          (transpose-key key mode)
        sharp?        (not (flat? key'))                    ;; TODO meh
        sharps'       (when sharp? (get sharps key' #{}))
        flats'        (when-not sharp? (get flats key' #{}))
        measure-notes (->> measure
                           calc-note-times
                           (map #(assoc % :key key' :mode mode :sharp? sharp?)))
        notes (reduce (fn [acc note]
                        (let [[note' accidentals] (note->abc note (select-keys acc [:sharps :flats]))]
                          (cond-> acc
                                  true
                                  (update :notes conj note')

                                  sharp?
                                  (assoc :sharps accidentals)

                                  (not sharp?)
                                  (assoc :flats accidentals))))
                      {:notes [] :sharps sharps' :flats flats'} measure-notes)]
    (str/join " " (:notes notes))))

(defn chromo->measures [chromo]
  (partition measure-sixteens chromo))

(defn chromo->measures-count [chromo]
  (count (chromo->measures chromo)))

(defn chromo->abc [chromo chord-names key mode]
  (let [measures (chromo->measures chromo)]
    (str/join " | " (map #(str "\\\"" %2 "\\\" " (measure->abc % key mode)) measures chord-names))))

(def c1 [60 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         62 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         64 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
         65 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2 -2
        ])

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

(defn chromatic-chromosome [key chords & {:keys [asc?] :or {asc? true}}]
  (let [root (key->int-note key)
        chord-count (count chords)
        root' (if (> chord-count 4)
                (+ root (if asc? -24 24))
                root)]
    (take (* chord-count measure-sixteens)
      (->> (iterate (if asc? inc dec) root')
        (map #(cons % [-2 -2 -2]))
        (apply concat)))))


(comment
  (key->int-note "G"))

(defn gen-track [{:keys [key measures mode]}]
  (let [root-note (key->int-note key)
        scale-notes (mode->scale mode)]
    (take (* 16 measures) (mapcat (fn [iv] [(+ root-note iv) -2 -2 -2]) (cycle scale-notes)))))

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

(defn chord->str [[root :as chord] key mode]
  (let [notes (if (sharp? (transpose-key key mode)) notes notes-flat)
        r (get notes (mod root 12))
        chord-ivs (chord-intervals chord)
        chord-str (chords chord-ivs)]
    (str r chord-str)))

(comment
  (chord->str [66 70 73 77] "C" "locrian")
  )

(defn gen-chord [{:keys [key mode duration degree chord octave-offset annotate-with-chord-name]
                  :or {octave-offset 0}}]
  (let [root-note (key->int-note key)
        root-note (+ root-note (* 12 octave-offset))
        scale-notes (intervals* (mode->scale mode))
        chord-notes (map #(+ root-note (nth scale-notes (+ degree %))) (get chord-intervals-map chord [0 2 4]))
        chord-str (chord->str chord-notes key mode)]
    (str (when annotate-with-chord-name (str "\\\"" chord-str "\\\" "))
      "[ "  (str/join (map str (map abc-note-map chord-notes) (repeat duration))) "]")
    ))

(comment
  (gen-chord {:key "C" :mode "major"
              :duration 8 :degree 0 :chord "R + 3 + 3"}))

(defn gen-chord-notes [{:keys [key mode duration degree chord]}]
  (let [root-note (key->int-note key)
        scale-notes (intervals* (mode->scale mode))
        chord-notes (map #(+ root-note (nth scale-notes (+ degree %))) (get chord-intervals-map chord [0 2 4]))]
    chord-notes))

(defn gen-scale-oct-notes
  "Scale octave notes e.g. for Gmajor => (0 2 4 6 7 9 11)"
  [key mode]
  (let [root-note   (key->int-note key)
        scale-notes (mode->scale mode)]
    (->> scale-notes
      (map #(+ root-note %))
      (map #(mod % 12))
      sort)))

(comment
  (gen-scale-oct-notes "G" "dorian"))

(defn gen-chord-2 [{:keys [key mode duration degree chord] :as params}] ;; yey and also, check duration
  (let [chord-notes (gen-chord-notes params)]
    (chord->str chord-notes key mode)))

(comment
  (gen-chord-notes {:key "C" :mode "major" :duration 8 :degree 0 :chord "R + 3 + 3 + 3"})
  (gen-chord-2 {:key "C" :mode "major" :duration 8 :degree 6 :chord "R + 3 + 3 + 3"}))

(def degrees {"I" 0
              "II" 1
              "III" 2
              "IV" 3
              "V" 4
              "VI" 5
              "VII" 6})

(defn progression->degrees [progression]
  (map degrees (str/split progression #"-")))

(comment
  (progression->degrees "I-I-I-I-IV-IV-I-I-V-V-I-I")
  )

(defn progression-measure-count [progression]
  (count (str/split progression #"-")))

(comment
  (progression-measure-count "I-IV-V-I"))

(comment
  (progression->degrees "I-IV-V-I"))

(defn gen-chord-progression [{:keys [key mode duration progression chord octave-offset repetitions] :or {octave-offset 0}}]
  (let [dgs (progression->degrees progression)
        chords (map #(gen-chord {:key key :mode mode :duration duration :octave-offset octave-offset
                                 :chord chord :degree %}) dgs)
        chords' (apply concat (repeat repetitions chords))]
    (str/join " | " chords')))

(defn gen-chord-progression-notes [{:keys [key mode duration progression chord repetitions]}]
  (let [dgs (progression->degrees progression)
        chords (map #(gen-chord-notes {:key key :mode mode :duration duration
                                       :chord chord :degree %}) dgs)]
    (apply concat (repeat repetitions chords))))

(comment
  (gen-chord-progression {:key "G" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 2})
  (gen-chord-progression-notes {:key "G" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1}))

(defn gen-chord-names [{:keys [mode progression repetitions] :as settings}]
  (let [dgs (progression->degrees progression)]
    (->> dgs
      (map #(gen-chord-2 (merge settings {:degree %})))
      (repeat repetitions)
      (apply concat))))

;; TODO move to separate ns

(defn rest-chromosome
  "A very tired chromosome"
  [settings]
  (let [names (gen-chord-names settings)
        n (count names)]
    (mapcat (fn [_] (concat [-1] (repeat 15 -2))) (repeat n -1))))

(defn random-track [{:keys [key mode] :as settings}]
  (let [measures     (count (gen-chord-names settings))
        root-note (key->int-note key)
        scale-notes (mode->scale mode)
        ;; one day this will be much more intelligent
        scale-notes* (apply concat (repeat 1000 scale-notes))]
    (take (* 16 measures) (mapcat (fn [iv] [(+ root-note iv) -2 -2 -2]) (shuffle scale-notes*)))))

(comment
  #_(count (gen-chord-names {:key "C" :progression all-degrees-progression :mode "major" :repetitions 2}))
  (random-track {:key "C" :progression all-degrees-progression :mode "major" :repetitions 2}))


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
  [{:keys [key mode progression chord tempo repetitions accompaniment] :as settings} {:keys [genes]}]
  (let [chord-names (gen-chord-names settings)]
    (str
      "X:1\\n"
      "K:" (->abc-key key mode) "\\n"
      "Q:" tempo "\\n"
      "V:V1 clef=treble \\n"
      (when (not= accompaniment "picking")
        "V:V2 clef=bass \\n")
      (str "[V:V1] | " (chromo->abc genes chord-names key mode) " | \\n")
      (when (not= accompaniment "picking")
        (str "[V:V2] | " (gen-chord-progression {:key           key :mode mode
                                                 :chord         chord
                                                 :duration      8 :progression progression
                                                 :repetitions   repetitions
                                                 :octave-offset -1})
          "|")))))

(comment
  (->abc-track {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :chord "R + 3 + 3 + 3"} {:genes c}))


(comment
  (->abc-track {:key "A" :mode "major" :duration 8 :progression "I-IV-V-I" :chord "R + 3 + 3 + 3"} {:genes (chromatic-chromosome 60)}))
