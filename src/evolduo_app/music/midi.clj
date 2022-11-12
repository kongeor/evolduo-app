(ns evolduo-app.music.midi)

;; https://en.wikipedia.org/wiki/General_MIDI
(def instruments [[4 "Honky-tonk Piano"]
                  [5 "Electric Piano 1"]
                  [11 "Music Box"]
                  [19 "Rock Organ"]
                  [41 "Violin"]
                  [49 "String Ensemble 1"]
                  [51 "Synth Strings 1"]
                  [82 "Synth Lead 2"]
                  [90 "Synth Lead 2"]
                  [91 "Synth Pad 3"]
                  [95 "Synth Pad 7"]
                  [108 "Koto"]
                  ])

(def instrument-keys (mapv first instruments))