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
  [:select {:name "chord" :id "chord-select"}
   (for [c music/chord-intervals-keys]
     [:option (merge {:value c}
                (when (= c chord)
                  {:selected true})) c])])

(defn note-type-select [note-type]
  [:select {:name "notes"}
   (for [t ["rests" "random" "asc" "desc"]]
     [:option (merge {:value t}
                (when (= t note-type)
                  {:selected true})) t])])

(defn rating-button [id text title value user-id rateable? not-rateable-msg reaction]
  [:p.control
   [:button.button.is-size-5
    (merge
      {:title title :onclick (str "document.getElementById('rating-value-" id "').value = " value " ; document.getElementById('rating-form-" id "').submit()")}
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
        ))
    [:span text]]])

(def rating-values
  [{:text "\uD83E\uDD22" :title "Oh my god!" :value -2}
   {:text "\uD83D\uDE41" :title "I don't like this" :value -1}
   {:text "\uD83D\uDE10" :title "I neither like nor dislike this" :value 0}
   {:text "\uD83D\uDE42" :title "I like this" :value 1}
   {:text "\uD83D\uDE03" :title "I like this a lot!" :value 2}
   ])

;;
(defn abc-track [{:keys [chromosome_id fitness raw_fitness abc]} & {:keys [evolution-id user-id reaction hide-reaction?
                                                                           is-admin? rateable? not-rateable-msg
                                                                           iteration-num]}]
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
     (when-not hide-reaction?
       [:form
        {:id (str "rating-form-" id) :action "/reaction" :method "POST"}
        [:input {:type "hidden" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
        [:input {:type "hidden" :name "chromosome_id" :value id}]
        [:input {:type "hidden" :name "redirect_url" :value (urls/url-for :iteration-detail {:evolution-id  evolution-id
                                                                                             :iteration-num iteration-num})}]
        [:input {:type "hidden" :id (str "rating-value-" id) :name "value" :value "-10"}]
        [:div.field.has-addons.mb-4
         (for [{:keys [text title value]} rating-values]
           (rating-button id text title value user-id rateable? not-rateable-msg reaction))]])
     [:button.button.is-light {:class download-midi-id} "Download MIDI"]
     [:button.button.is-light {:class download-wav-id} "Download WAV"]
     [:hr.mb-4]]))

(defn pagination [{:keys [current max link-fn]}]
  [:div
   [:h3.is-size-4.mb-4 "Jump to iteration"]
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
           {:href         (link-fn i)
            :aria-label   (str "Page " i)
            :aria-current "page"}
           (when (= i current)
             {:class "is-current"})) (str i)]])]]])

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
   (for [k ["my" "friends" "public"]]
     [:option (merge {:value k}
                (when (= k type)
                  {:selected true})) k])])
