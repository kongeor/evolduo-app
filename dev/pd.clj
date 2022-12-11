(ns pd
  (:require [clojure.string :as str]
            [evolduo-app.music :as mu])
  (:import (java.io DataInputStream DataOutputStream)
           (java.net Socket)))

(def address "127.0.0.1")
(def port 3000)
(def port2 3001)
#_(def command "108 105 115 116 32 49 48 32 50 48 32 51 48 59 10;")
(def socket (Socket. address port))
(def socket2 (Socket. address port2))

#_(def in (DataInputStream. (.getInputStream socket)))
(def out (DataOutputStream. (.getOutputStream socket)))
(def out2 (DataOutputStream. (.getOutputStream socket2)))

#_(println "Input:" command)

(defn note->cmd [pitch duration]
  (str (str/join " " (map int (str "list " pitch " 100 " duration))) ";"))

(defn chord-cmd [duration pitches]
  (str
    (->> (map #(str % " 100 " %2) pitches (repeat duration))
      (str/join " ")
      (map int)
      (str/join " "))
    ";")
  #_(str (str/join " "  ";"))
  #_(str (str/join " " (map int (str "list " pitch " 100 " duration))) ";"))

(comment
  (chord-cmd 1000 [60 64]))

#_(.writeBytes out (note->cmd 60))

(def bpm 60)

;; with 60 bmp, a whole note (16) should produce a duration of 4000 ms

(defn duration->ms [bmp duration]
  (let [d (float (* 4 (/ duration mu/measure-sixteens)))
        b (float (/ 60 bmp))]
    (int (* d b 1000))))

(comment
  (duration->ms 60 4))

(comment
  (/ 4000 16))

(defn play-note [bmp {:keys [note duration] :as note}]
  (let [duration-ms (duration->ms bmp duration)
        cmd (note->cmd note duration-ms)]
    (println "playing note" note duration)
    (.writeBytes out cmd)))

(defn play-track [bmp chromosome]
  (let [notes (group-by :index (mu/calc-note-times chromosome))]
    (doseq [i (range (count chromosome))]
      (when-let [n (first (get notes i))]
        (play-note bmp n))
      (Thread/sleep 250))))

(comment
  (play-track bpm mu/c))

(defn play-chord [out bmp chord]
  (.writeBytes out (chord-cmd 4000 chord)))

(comment
  (play-chord out2 60 [60 64 67]))

(defn play-progression [out bmp progression]
  (doseq [chord progression]
    (play-chord out bmp chord)
    (Thread/sleep 4000)))

(comment
  (let [prog (mu/gen-chord-progression-notes {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1})]
    (play-progression out2 60 prog)))

(comment
  (let [prog (mu/gen-chord-progression-notes {:key "C" :mode "major" :duration 8 :progression "I-IV-V-I" :repetitions 1})]
    (future (play-progression out2 60 prog))
    (play-track bpm mu/c)))

(comment
  (let [track (mu/random-track {:key "C" :progression "I-IV-V-I" :mode "major" :repetitions 2 :chord "R"})]
    (play-track bpm track)))

(comment
  (doseq [p (take 20 (cycle [57 60 62 64 67]))]
    (.writeBytes out (note->cmd p))
    (Thread/sleep 500)))

#_(def response (.readLine in))
#_(println "Output: " response)


(comment
  (map (comp int) (seq "list 57 60 1000")))
