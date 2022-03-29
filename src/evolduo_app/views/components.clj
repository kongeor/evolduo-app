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

;;
(defn abc-track [{:keys [id abc]}]
  (let [abc-id (str "abc_" id)
        abc-activate (str "activate-audio-" id)
        abc-stop (str "stop-audio-" id)
        abc-start-measure-id (str "start-measure-" id)
        abc-end-measure-id (str "end-measure-" id)]
    [:div
     [:script {:type "text/javascript"}
      (str "var " abc-id " = \"" abc "\";")]
     [:div.abc-track {:style "display: none"} id]
     [:h3.title.is-size-3 (str "#" id)]
     [:div {:id abc-id}]
     [:div.buttons
      [:button.button.is-primary {:class abc-activate} "Play"]
      [:button.button.is-light {:class abc-stop} "Stop"]
      #_[:div.suspend-explanation]
      [:div {:id abc-start-measure-id}]
      [:div {:id abc-end-measure-id}]
      ]
     [:hr.mb-4]]))