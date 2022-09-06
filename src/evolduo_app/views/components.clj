(ns evolduo-app.views.components
  (:require [evolduo-app.music :as music]
            [evolduo-app.schemas :as s]
            [ring.middleware.anti-forgery :as anti-forgery]
            [clojure.contrib.humanize :as h]
            [evolduo-app.urls :as urls]))

(defn select [name selected options]
  [:select {:name name}
   (for [o options]
     [:option (merge {:value o}
                (when (= o selected)
                  {:selected true})) o])])

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

(defn keys-select-restricted [key]
  [:select {:name "key"}
   (for [k music/music-keys-restricted]
     [:option (merge {:value k}
                     (when (= k key)
                       {:selected true})) k])])

(defn mode-select [mode]
  [:select {:name "mode"}
   (for [m music/mode-names]
     [:option (merge {:value m}
                (when (= m mode)
                  {:selected true})) m])])

(defn progression-select [progression]
  [:select {:name "progression"}
   (for [p music/progressions]
     [:option (merge {:value p}
                (when (= p progression)
                  {:selected true})) p])])

(defn chord-select [chord]
  [:select {:name "chord"}
   (for [c music/chord-intervals-keys]
     [:option (merge {:value c}
                (when (= c chord)
                  {:selected true})) c])])

;;
(defn abc-track [{:keys [chromosome_id fitness abc]} & {:keys [evolution-id user-id reaction hide-reaction?]}]
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
     (when fitness
       [:p (str "Fitness: " fitness)])
     [:div {:id abc-id}]
     [:div.mb-4 {:id audio-id}]
     [:div.buttons
      [:button.button.is-primary {:class abc-activate} "Play"]
      [:button.button.is-light {:class abc-stop} "Stop"]
      [:button.button.is-light {:class download-midi-id} "Get Midi"]
      [:button.button.is-light {:class download-wav-id} "Get Wav"]
      [:div {:id abc-start-measure-id}]
      [:div {:id abc-end-measure-id}]]
     (when-not hide-reaction?
       [:div.buttons
        [:form
         {:action "/reaction" :method "POST"}
         [:input {:type "hidden" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
         [:input {:type "hidden" :name "chromosome_id" :value id}]
         [:input {:type "hidden" :name "redirect_url" :value (urls/url-for :evolution-detail {:evolution-id evolution-id})}]
         [:input {:type "hidden" :name "value" :value "1"}]
         [:input.button.is-link.mr-2 (merge
                                       {:type "submit" :value "Nice!"}
                                       (when (or reaction (not user-id))
                                         {:disabled true})
                                       (when reaction
                                         {:title "You have already rated this track"})
                                       (when (not user-id)
                                         {:title "You need to be logged in to rate this track"}))]]
        [:form
         {:action "/reaction" :method "POST"}
         [:input {:type "hidden" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
         [:input {:type "hidden" :name "chromosome_id" :value id}]
         [:input {:type "hidden" :name "redirect_url" :value (urls/url-for :evolution-detail {:evolution-id evolution-id})}]
         [:input {:type "hidden" :name "value" :value "-1"}]
         [:input.button.is-warning (merge
                                     {:type "submit" :value "Meh!"}
                                     (when (or reaction (not user-id))
                                       {:disabled true})
                                     (when reaction
                                       {:title "You have already rated this track"})
                                     (when (not user-id)
                                       {:title "You need to be logged in to rate this track"}))]]])
     [:hr.mb-4]]))

(defn pagination [{:keys [current max link-fn]}]
  [:nav.pagination {:role "navigation" :aria-label "pagination"}
   [:a.pagination-previous
    (if (zero? current)
      {:disabled true}
      {:href (link-fn (dec current))}) "Previous"]
   [:a.pagination-next
    (if (= current max)
      {:disabled true}
      {:href (link-fn (inc current))}) "Next"]
   [:ul.pagination-list
    (for [i (range 0 (inc max))]
      [:li
       [:a.pagination-link
        (merge
          {:href (link-fn i)
           :aria-label (str "Page " i)
           :aria-current "page"}
          (when (= i current)
            {:class "is-current"})) (str i)]])]])

(defn evolution-table [evolutions]
  [:table.table.is-fullwidth
   [:thead
    [:tr
     [:th "Id"]
     [:th "Created"]
     [:th "Updated"]
     [:th "Progress"]
     [:th "Key"]
     [:th "Mode"]
     [:th "Progression"]
     [:th "Reps."]
     [:th "Chord"]
     ]]
   [:tbody
    (for [e evolutions]
      (let [id (:evolution_id e)
            perc (abs (* 100 (/ (:num e) (:total_iterations e))))
            perc-str (str perc "%")]
        [:tr
         [:td [:a {:href (str "/evolution/" id)} id]]
         [:td {:title (:created_at e)} (h/datetime (:created_at e))]
         [:td {:title (:updated_at e)} (h/datetime (:updated_at e))]
         [:td [:progress.progress {:value (:num e) :max (:total_iterations e)} perc-str]]
         [:td (:key e)]
         [:td (:mode e)]
         [:td (:progression e)]
         [:td (:repetitions e)]
         [:td (:chord e)]
         ]))]])                           ;; TODO add a date

(defn evolution-type-select [type]
  [:select {:name "type"}
   (for [k ["my" "invited" "public"]]
     [:option (merge {:value k}
                (when (= k type)
                  {:selected true})) k])])
