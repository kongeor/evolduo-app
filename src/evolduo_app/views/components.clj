(ns evolduo-app.views.components
  (:require [evolduo-app.music :as music]
            [ring.middleware.anti-forgery :as anti-forgery]))

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
        abc-end-measure-id (str "end-measure-" id)
        audio-id (str "audio-" id)
        download-midi-id (str "download-midi-" id)
        download-wav-id (str "download-wav-" id)
        ]
    [:div
     [:script {:type "text/javascript"}
      (str "var " abc-id " = \"" abc "\";")]
     [:div.abc-track {:style "display: none"} id]
     [:h3.title.is-size-3 (str "#" id)]
     [:div {:id abc-id}]
     [:div.mb-4 {:id audio-id}]
     [:div.buttons
      [:button.button.is-primary {:class abc-activate} "Play"]
      [:button.button.is-light {:class abc-stop} "Stop"]
      [:button.button.is-light {:class download-midi-id} "Get Midi"]
      [:button.button.is-light {:class download-wav-id} "Get Wav"]
      [:div {:id abc-start-measure-id}]
      [:div {:id abc-end-measure-id}]]
     [:div.buttons
      [:form
       {:action "/reaction" :method "POST"}
       [:input {:type "hidden" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
       [:input {:type "hidden" :name "chromosome_id" :value id}]
       [:input {:type "hidden" :name "type" :value "rating"}]
       [:input {:type "hidden" :name "value" :value "1"}]
       [:input.button.is-link {:type "submit" :value "Nice!"}]]]
     [:hr.mb-4]]))