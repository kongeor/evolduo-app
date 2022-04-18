(ns evolduo-app.views.components
  (:require [evolduo-app.music :as music]
            [evolduo-app.schemas :as s]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn evolve-after-select [evolve-after]
  [:select {:name "evolve_after"}
   (for [a s/evolve-after-options]
     [:option (merge {:value a}
                (when (= a evolve-after)
                  {:selected true})) a])])

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
(defn abc-track [{:keys [chromosome_id abc]} & {:keys [reaction]}]
  (let [id chromosome_id
        abc-id (str "abc_" id)
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
     [:h3.title.is-size-4 (str "#" id)]
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
       [:input.button.is-link (merge
                                {:type "submit" :value "Nice!"}
                                (when reaction
                                  {:class "is-selected"
                                   :disabled true}))]]]
     [:hr.mb-4]]))

(defn pagination [{:keys [current max link-fn]}]
  [:nav.pagination {:role "navigation" :aria-label "pagination"}
   [:a.pagination-previous.is-disabled {:title "This is the first page"} "Previous"]
   [:a.pagination-next "Next page"]
   [:ul.pagination-list
    (for [i (range 1 (inc max))]
      [:li
       [:a.pagination-link
        (merge
          {:href (link-fn i)
           :aria-label (str "Page " i)
           :aria-current "page"}
          (when (= i current)
            {:class "is-current"})) (str i)]])]])