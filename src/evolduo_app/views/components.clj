(ns evolduo-app.views.components
  (:require [evolduo-app.music :as music]))

(defn keys-select [key]
  [:select {:name "key"}
   (for [k music/music-keys]
     [:option (merge {:value k}
                (when (= k key)
                  {:selected true})) k])])

(defn mode-select [mode]
  [:select {:name "mode"}
   (for [m music/modes]
     [:option (merge {:value m}
                (when (= m mode)
                  {:selected true})) m])])

(defn pattern-select [pattern]
  [:select {:name "pattern"}
   (for [p music/patterns]
     [:option (merge {:value p}
                (when (= p pattern)
                  {:selected true})) p])])

(defn chord-select [chord]
  [:select {:name "chord"}
   (for [c music/chord-intervals-keys]
     [:option (merge {:value c}
                (when (= c chord)
                  {:selected true})) c])])

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