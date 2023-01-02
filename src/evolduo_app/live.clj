(ns evolduo-app.live
  (:require [clojure.string :as str]
            [evolduo-app.music :as mu]
            [evolduo-app.model.evolution :as evolution]
            [evolduo-app.model.iteration :as iteration])
  (:import (java.io DataInputStream DataOutputStream)
           (java.net Socket)
           (javax.swing JButton JLabel JSlider JComboBox JPanel JFrame)
           (java.awt GridLayout)
           (javax.swing.event ChangeListener ChangeEvent)
           (java.awt.event ActionListener)))

(def address "127.0.0.1")
(def port 3000)
(def port2 3001)
#_(def command "108 105 115 116 32 49 48 32 50 48 32 51 48 59 10;")
#_(def socket (Socket. address port))
#_(def socket2 (Socket. address port2))

#_(def in (DataInputStream. (.getInputStream socket)))
#_(def out (DataOutputStream. (.getOutputStream socket)))
#_(def out2 (DataOutputStream. (.getOutputStream socket2)))

(defn make-data-out-stream [port]
  (let [socket (Socket. address port)]
    (DataOutputStream. (.getOutputStream socket))))

#_(println "Input:" command)

(defn note->cmd [pitch duration]
  (str (str/join " " (map int (str "list " pitch " 100 " duration))) ";"))

(defn chord-cmd [duration velocity pitches]
  (let [pitches' (concat pitches (repeat (- 4 (count pitches)) 0))]
    (str
      (->> (map #(str % " " velocity " " %2) pitches' (repeat duration))
        (str/join " ")
        (map int)
        (str/join " "))
      ";"))
  #_(str (str/join " "  ";"))
  #_(str (str/join " " (map int (str "list " pitch " 100 " duration))) ";"))

(comment
  (chord-cmd 1000 100 [60 64 67 69]))

#_(.writeBytes out (note->cmd 60))

;; with 60 bmp, a whole note (16) should produce a duration of 4000 ms

(defn duration->ms [bpm duration]
  (let [d (float (* 4 (/ duration mu/measure-sixteens)))
        b (float (/ 60 bpm))]
    (int (* d b 1000))))

(defn sleep [^long duration]
  (Thread/sleep duration))

(comment
  (duration->ms 120 16))

(comment
  (comment
    (/ 4000 16)))

(defn play-note [out bpm {:keys [note duration]}]
  (let [duration-ms (duration->ms bpm duration)
        cmd (note->cmd note duration-ms)]
    (println "playing note" note duration)
    (.writeBytes out cmd)))

(defn play-track [out bpm chromosome]
  (let [notes (group-by :index (mu/calc-note-times chromosome))]
    (doseq [i (range (count chromosome))]
      (when-let [n (first (get notes i))]
        (play-note out bpm n)
        (sleep (duration->ms bpm (:duration n)))))))

(comment
  (play-track nil bpm mu/c))

(defn play-chord [out bmp attack chord]
  (let [duration-ms (duration->ms bmp mu/measure-sixteens)]
    (.writeBytes out (chord-cmd duration-ms attack chord))))

(comment
  (play-chord (make-data-out-stream port2) 60 [60 67 72]))

(defn play-progression [out bpm progression]
  (doseq [chord progression]
    (play-chord out bpm 100 chord)
    (sleep (duration->ms bpm mu/measure-sixteens))))

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

(def rest-vec (comp vec rest))

(def state (atom {:status :running
                  ; :out (make-data-out-stream port)
                  ; :out2 (make-data-out-stream port2)
                  :buffer []
                  :config {:key             "C"
                           :mode            "major"
                           :duration        8
                           :progression     "I-IV-II-V"
                           :crossover_rate  30
                           :mutation_rate   30
                           :tempo           100
                           :repetitions     1
                           :chord           "R + 3 + 3 + 3"
                           :chord-velocity  100
                           :population_size 40}}))

(defn connect! []
  (swap! state assoc :out (make-data-out-stream port))
  (swap! state assoc :out2 (make-data-out-stream port2))
  )

(comment
  (swap! state update :config assoc :mutation_rate 80)
  (swap! state update :config assoc :tempo 100))

(comment
  (mu/gen-chord-progression-notes (-> @state :config)))

(defn loop-play-track []
  (future
    (loop []
      (when (= :running (:status @state))
        (let [s @state
              genes-to-play (->> s :buffer first
                              (sort-by :fitness)
                              first
                              :genes)
              _ (swap! state update :buffer rest-vec)
              genes-by-index (group-by :index (mu/calc-note-times genes-to-play))
              tempo (-> s :config :tempo)
              chord-velocity (-> s :config :chord-velocity)
              out (:out s)
              out2 (:out2 s)
              progression (mu/gen-chord-progression-notes (-> s :config))
              progression-indexed (apply hash-map (interleave (iterate (partial + mu/measure-sixteens) 0) progression))]
          (doseq [tick (range (count genes-to-play))]
            #_(println "looping tick" tick)
            (when-let [note (-> genes-by-index (get tick) first)]
              (println "playing note" note)
              (play-note out tempo note))
            (when-let [chord (-> progression-indexed (get tick))]
              (println "playing chord" chord)
              (play-chord out2 tempo chord-velocity chord))
            (sleep (duration->ms tempo 1))
            #_(println "tempo" tempo "genes" genes-to-play)
            #_(future (play-progression (:out2 @state) tempo progression))
            #_(play-track (:out @state) tempo genes-to-play)
            #_(println "looping")))
      (recur)))))


(comment
  (dissoc @state :buffer)
  (-> @state :buffer count))

(comment
  (loop-play-track))

(comment
  (play-chord (:out2 @state) 60 [60 64 67]))

(defn loop-evolve-new-tracks []
  (future
    (try
      (loop []
        (when (= :running (:status @state))
          (if (> 2 (-> @state :buffer count))
            (do
              (println "evolving...")
              (let [new-track (iteration/chickn-evolve (:config @state) (->> @state
                                                                          :buffer
                                                                          last))]
                (println "new fitness: " (:fitness (first new-track)))
                (swap! state update :buffer conj new-track)))
            (do
              #_(println "waiting...")
              (sleep 1000)))
          (recur)))
      (catch Exception e
        (println "oh no...." e)))
    ))

(defn cancel-futures []
  (when-let [new-tracks-loop (:new-tracks-loop @state)]
    (future-cancel new-tracks-loop))
  (when-let [track-playback-loop (:track-playback-loop @state)]
    (future-cancel track-playback-loop)))

(comment
  (cancel-futures))

(defn restart []
  (cancel-futures)
  (swap! state update :buffer empty)
  ;; initialize first
  (swap! state update :buffer conj (evolution/generate-initial-chromosomes (:config @state)))
  (swap! state assoc :new-tracks-loop (loop-evolve-new-tracks))
  (swap! state assoc :track-playback-loop (loop-play-track)))

(comment
  (restart))

(comment
  (loop-evolve-new-tracks))

(comment
  (swap! state assoc :status :stopped)
  (swap! state assoc :status :running))

(comment
  )

;; https://www.reddit.com/r/linuxaudio/comments/jsrl31/what_do_midi_through_ports_come_from_and_can_i/


;; swing stuff

(defn set-default-slider-options [slider]
  (.setPaintLabels slider true)
  (.setPaintTicks slider true)
  (.setMinorTickSpacing slider 10)
  (.setMajorTickSpacing slider 10)
  (.setSnapToTicks slider true))

;; define audio control buttons
(def start-button (JButton. "Start"))
(def stop-button (JButton. "Stop"))
(def rewind-button (JButton. "Rewind"))

;; define settings components
(def crossover-label (JLabel. "Crossover:"))
(def crossover-slider (JSlider. 0 100 30))
(set-default-slider-options crossover-slider)
(def mutation-label (JLabel. "Mutation:"))
(def mutation-slider (JSlider. 0 100 30))
(set-default-slider-options mutation-slider)
(def tempo-label (JLabel. "Tempo:"))
(def tempo-slider (JSlider. 60 220 100))
(set-default-slider-options tempo-slider)
(def chord-velocity-label (JLabel. "Chord velocity:"))
(def chord-velocity-slider (JSlider. 0 100 100))
(set-default-slider-options chord-velocity-slider)

(def key-label (JLabel. "Key:"))
(def key-dropdown (JComboBox. (into-array String mu/music-keys)))
(def mode-label (JLabel. "Mode:"))
(def mode-dropdown (JComboBox. (into-array String mu/mode-names)))
(def progression-label (JLabel. "Progression:"))
(def progression-dropdown (JComboBox. (into-array String mu/progressions)))
(.setSelectedItem progression-dropdown (-> @state :config :progression))

(def chord-label (JLabel. "Chord:"))
(def chord-dropdown (JComboBox. (into-array String mu/chord-intervals-keys)))
(.setSelectedItem chord-dropdown (-> @state :config :chord))


;; define main panel and add audio control buttons and settings components
(def panel (JPanel. (GridLayout. 9 2)))
(.add panel start-button)
(.add panel stop-button)
#_(.add panel rewind-button)
(.add panel crossover-label)
(.add panel crossover-slider)
(.add panel mutation-label)
(.add panel mutation-slider)
(.add panel chord-velocity-label)
(.add panel chord-velocity-slider)
(.add panel tempo-label)
(.add panel tempo-slider)
(.add panel key-label)
(.add panel key-dropdown)
(.add panel mode-label)
(.add panel mode-dropdown)
(.add panel progression-label)
(.add panel progression-dropdown)
(.add panel chord-label)
(.add panel chord-dropdown)

(defn set-enabled-controls [b]
  (.setEnabled key-dropdown b)
  (.setEnabled mode-dropdown b)
  (.setEnabled progression-dropdown b))

(def start-listener (reify ActionListener
                     (actionPerformed [this e]
                       (println "Restarting...")
                       (set-enabled-controls false)
                       (restart))))

(.addActionListener start-button start-listener)

(def stop-listener (reify ActionListener
                      (actionPerformed [this e]
                        (println "The future cancels ... now!")
                        (set-enabled-controls true)
                        (cancel-futures))))

(.addActionListener stop-button stop-listener)

(def crossover-listener (reify ChangeListener
                          (stateChanged [this e]
                            (let [crossover (.getValue crossover-slider)]
                              (println "changing crossover to" crossover)
                              (swap! state update :config assoc :crossover crossover)))))

(.addChangeListener crossover-slider crossover-listener)

(def mutation-listener (reify ChangeListener
                          (stateChanged [this e]
                            (let [mutation (.getValue mutation-slider)]
                              (println "changing mutation to" mutation)
                              (swap! state update :config assoc :mutation mutation)))))

(.addChangeListener mutation-slider mutation-listener)

(def chord-velocity-listener (reify ChangeListener
                             (stateChanged [this e]
                               (let [chord-velocity (.getValue chord-velocity-slider)]
                                 (println "changing chord velocity to" chord-velocity)
                                 (swap! state update :config assoc :chord-velocity chord-velocity)))))

(.addChangeListener chord-velocity-slider chord-velocity-listener)

(def tempo-listener (reify ChangeListener
                     (stateChanged [this e]
                       (let [tempo (.getValue tempo-slider)]
                         (println "changing tempo to" tempo)
                         (swap! state update :config assoc :tempo tempo)))))

#_(.removeChangeListener tempo-slider tempo-listener)
(.addChangeListener tempo-slider tempo-listener)

(def key-listener (reify ActionListener
                     (actionPerformed [this e]
                       (let [key (.getSelectedItem key-dropdown)]
                         (println "setting key to" key)
                         (swap! state update :config assoc :key key)))))

(.addActionListener key-dropdown key-listener)

(def mode-listener (reify ActionListener
                     (actionPerformed [this e]
                       (let [mode (.getSelectedItem mode-dropdown)]
                         (println "setting mode to" mode)
                         (swap! state update :config assoc :mode mode)))))

(.addActionListener mode-dropdown mode-listener)
#_(.removeActionListener mode-dropdown mode-listener)

(def progression-listener (reify ActionListener
                            (actionPerformed [this e]
                              (let [progression (.getSelectedItem progression-dropdown)]
                                (println "setting progression to" progression)
                                (swap! state update :config assoc :progression progression)))))

(.addActionListener progression-dropdown progression-listener)
#_(.removeActionListener progression-dropdown progression-listener)

(def chord-listener (reify ActionListener
                            (actionPerformed [this e]
                              (let [chord (.getSelectedItem chord-dropdown)]
                                (println "setting chord to" chord)
                                (swap! state update :config assoc :chord chord)))))

(.addActionListener chord-dropdown chord-listener)

;; define main frame and add main panel
(defn show-controls! []
  (let [frame (JFrame. "Evolduo Live!")]
    (.add frame panel)
    (.setSize frame 800 600)
    (.setVisible frame true)))

(comment
  (show-controls!))

(comment
  (connect!))

(defn connect-and-show-controls! []
  (connect!)
  (show-controls!))

(comment
  (connect-and-show-controls!))