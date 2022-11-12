(ns evolduo-app.music.midi)

;; https://en.wikipedia.org/wiki/General_MIDI
(def instruments [[4 "Honky-tonk Piano"]
                  [5 "Electric Piano 1"]
                  [11 "Music Box"]
                  [82 "Synth Lead 2"]
                  [90 "Synth Lead 2"]
                  [91 "Synth Pad 3"]
                  [108 "Koto"]
                  ])

(def instrument-keys (mapv first instruments))

;; randomize excluded the previous default instrument
(def rnd-instrument-keys (vec (disj (set instrument-keys) 4)))