(ns evolduo-app.music.operators
  (:require [evolduo-app.music :as muse]))

(defn calc-note-length [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (inc (count (take-while #(= % -2) (drop (inc note-idx) c)))))

(defn split-note [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (let [l (calc-note-length c note-idx)]
    (if (= l 1)
      c
      (let [p  (c note-idx)
            p' (+ -2 (rand-int 5) p)]
        (assoc c (+ note-idx (/ l 2)) p')))))

(defn next-note-idx [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (let [idx (+ note-idx (inc (count (take-while #(= % -2) (drop (inc note-idx) c)))))]
    (if (>= idx (count c))
      nil
      idx)))

(comment
  (next-note-idx c1 0))

(defn merge-note [c note-idx]
  (assert (not= -2 (c note-idx)) (str "note a note at idx " note-idx " on " c))
  (let [l1 (calc-note-length c note-idx)
        idx2 (next-note-idx c note-idx)]
    (if idx2
      (let [l2 (calc-note-length c idx2)]
        (if (= l1 l2)
          (assoc c idx2 -2)
          c))
      c)))

(defn note? [n]
  (when (and
          (not= n -1)
          (not= n -2))
    n))

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
  #_(chromo->measures c)
  (muse/calc-note-times (second (muse/chromo->measures muse/c))))

(defn find-note-idxs [c]
  (loop [i 0
         n #{}]
    (if (= (count c) i)
      n
      (recur (inc i) (if (note? (c i))
                       (conj n i)
                       n)))))

(defn alter-random-note-pitch [c]
  (let [n (find-note-idxs c)
        i (rand-nth (vec n))
        r (rand-int 4)]
    (update c i +
      (condp = r
        0 -2
        1 -1
        2 1
        3 2))))

(defn split-random-note [c]
  (let [times (muse/calc-note-times c)
        note (->> times
               (filter #(note? (:note %)))
               (filter #(>= (:duration %) 4))
               shuffle
               first)]
    (if note
      (split-note c (:index note))
      c)))

(comment
  (split-random-note muse/c1))

(defn merge-random-note [c]
  (let [times (muse/calc-note-times c)
        note (->> times
               (filter #(note? (:note %)))
               (filter #(<= (:duration %) 4))
               shuffle
               first)]
    (if note
      (merge-note c (:index note))
      c)))

(comment
  (split-random-note muse/c1))
