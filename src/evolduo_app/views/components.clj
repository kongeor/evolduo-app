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
(defn abc-track [{:keys [chromosome_id fitness raw_fitness abc]} & {:keys [evolution-id user-id reaction hide-reaction?
                                                                           is-admin? rateable? not-rateable-msg]}]
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
     (when (and fitness is-admin?)
       [:div
        [:p (str "Fitness: " fitness)]
        [:p (str "Raw Fitness: " raw_fitness)]])
     [:div {:id abc-id}]
     [:div.mb-4 {:id audio-id}]
     [:div.buttons
      #_[:div.dropdown {:id (str "dropdown-" id)}
       [:div.dropdown-trigger
        [:button.button {:aria-haspopup "true" :aria-controls "dropdown-menu"}
         [:span {:data-target (str "dropdown-" id)} "Download "]
         #_[:span.icon.is-small
          [:svg {:xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 384 512"} [:path {:d "M192 384c-8.188 0-16.38-3.125-22.62-9.375l-160-160c-12.5-12.5-12.5-32.75 0-45.25s32.75-12.5 45.25 0L192 306.8l137.4-137.4c12.5-12.5 32.75-12.5 45.25 0s12.5 32.75 0 45.25l-160 160C208.4 380.9 200.2 384 192 384z"}]]]
         #_[:span.icon.is-small
          [:i.fas.fa-angle-down {:aria-hidden "true"}]]]]
       [:div#dropdown-menu.dropdown-menu {:role "menu"}
        [:div.dropdown-content
         [:a.dropdown-item {:class download-midi-id} "MIDI"]
         [:a.dropdown-item {:class download-wav-id} "WAV"]]]]]
     [:div.buttons
      #_[:button.button.is-primary {:class abc-activate} "Play"]
      #_[:button.button.is-light {:class abc-stop} "Stop"]
      #_[:div {:id abc-start-measure-id}]
      #_[:div {:id abc-end-measure-id}]]
     [:div.buttons
      (when-not hide-reaction?
        [:form
         {:action "/reaction" :method "POST"}
         [:input {:type "hidden" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
         [:input {:type "hidden" :name "chromosome_id" :value id}]
         [:input {:type "hidden" :name "redirect_url" :value (urls/url-for :evolution-detail {:evolution-id evolution-id})}]
         [:input {:type "hidden" :name "value" :value "1"}]
         [:input.button.is-link.mr-2 (merge
                                       {:type "submit" :value "I like this \uD83D\uDC4D"}
                                       (when (or reaction (not user-id) (not rateable?))
                                         {:disabled true})
                                       (cond
                                         (not user-id)
                                         {:title "You need to be logged in to rate this track"}

                                         (not rateable?)
                                         {:title not-rateable-msg}

                                         reaction
                                         {:title "You have already rated this track"}

                                         :else nil
                                         ))]])
      (when-not hide-reaction?
        [:form
         {:action "/reaction" :method "POST"}
         [:input {:type "hidden" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
         [:input {:type "hidden" :name "chromosome_id" :value id}]
         [:input {:type "hidden" :name "redirect_url" :value (urls/url-for :evolution-detail {:evolution-id evolution-id})}]
         [:input {:type "hidden" :name "value" :value "-1"}]
         [:input.button.is-danger.mr-4 (merge
                                    {:type "submit" :value "I don't like this \uD83D\uDC4E"}
                                    (when (or reaction (not user-id) (not rateable?))
                                      {:disabled true})
                                    (cond
                                      (not user-id)
                                      {:title "You need to be logged in to rate this track"}

                                      (not rateable?)
                                      {:title not-rateable-msg}

                                      reaction
                                      {:title "You have already rated this track"}

                                      :else nil
                                      ))]])
      [:button.button.is-light {:class download-midi-id} "Download MIDI"]
      [:button.button.is-light {:class download-wav-id} "Download WAV"]
      ]
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
         [:td [:progress.progress {:title (str (:num e) "/" (:total_iterations e)) :value (:num e) :max (:total_iterations e)} perc-str]]
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
