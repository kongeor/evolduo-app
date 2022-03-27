(ns evolduo-app.views.components
  (:require [evolduo-app.music :as music]))

;; TODO refer from music
(def music-keys ["A" "A#" "B" "C" "C#" "D" "D#" "E" "F" "F#" "G" "G#"])
(def modes ["major" "minor"])

(defn keys-select [key]
  [:select {:name "key"}
   (for [k music-keys]
     [:option (merge {:value k}
                (when (= k key)
                  {:selected true})) k])])

(defn mode-select [mode]
  [:select {:name "mode"}
   (for [m modes]
     [:option (merge {:value m}
                (when (= m mode)
                  {:selected true})) m])])

(defn pattern-select [pattern]
  [:select {:name "pattern"}
   (for [p music/patterns]
     [:option (merge {:value p}
                (when (= p pattern)
                  {:selected true})) p])])
