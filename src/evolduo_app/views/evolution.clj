(ns evolduo-app.views.evolution
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]
            [ring.middleware.anti-forgery :as anti-forgery]
            [evolduo-app.urls :as u]
            [evolduo-app.schemas :as s]
            [clojure.contrib.humanize :as h]))

(defn evolution-form [req {:keys [evolution errors] :as content}]
  (base-view
    req
    [:div
     [:h3.title.is-3 "New Evolution"]
     [:form {:method "post" :action "/evolution/save"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:div.control
        [:label.checkbox
         [:input (merge {:type "checkbox" :name "public" :value "true"}
                   (when (:public evolution)
                     {:checked "checked"}))]
         " Public"]]]
      [:div.field
       [:label.label {:for "min_ratings"} "Min Ratings"]
       [:div.control
        [:input.input {:type "number" :name "min_ratings" :value (:min_ratings evolution) :min "0"}]]]
      [:div.field
       [:label.label {:for "evolve_after"} "Evolve After"]
       [:div.control
        [:div.select
         (comps/evolve-after-select (:evolve_after evolution))]]
       (when-let [key-errors (:evolve_after errors)]
         [:p.help.is-danger (first key-errors)])]
      [:div.field
       [:label.label {:for "initial_iterations"} "Initial Iterations"]
       [:div.control
        [:input.input {:type "number" :name "initial_iterations" :value (:initial_iterations evolution) :min "0" :max "20"}]]]
      [:div.field
       [:label.label {:for "total_iterations"} "Total Iterations"]
       [:div.control
        [:input.input {:type "number" :name "total_iterations" :value (:total_iterations evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "population_size"} "Population Size"]
       [:div.control
        [:input.input {:type "number" :name "population_size" :value (:population_size evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "crossover_rate"} "Crossover Rate"]
       [:div.control
        [:input.input {:type "number" :name "crossover_rate" :value (:crossover_rate evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "mutation_rate"} "Mutation Rate"]
       [:div.control
        [:input.input {:type "number" :name "mutation_rate" :value (:mutation_rate evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "key"} "Key"]
       [:div.control
        [:div.select
         (comps/keys-select-restricted (:key evolution))]]
       [:p.help.is-info [:span
                         "Key selection is disabled for now. Read more about this limitation "
                         [:a {:href "#"} "here"]
                         "."]]
       (when-let [key-errors (:key errors)]
         [:p.help.is-danger (first key-errors)])]
      [:div.field
       [:label.label {:for "mode"} "Mode"]
       [:div.control
        [:div.select
         (comps/mode-select (:mode evolution))]]
       (when-let [key-errors (:mode errors)]
         [:p.help.is-danger (first key-errors)])]
      [:div.field
       [:label.label {:for "progression"} "Progression"]
       [:div.control
        [:div.select
         (comps/progression-select (:progression evolution))]]]
      [:div.field
       [:label.label {:for "repetitions"} "Repetitions"]
       [:div.control
        [:div.select
         (comps/select "repetitions" (:progression evolution) s/repetition-options)]]]
      [:div.field
       [:label.label {:for "chord"} "Chord Intervals"]
       [:div.control
        [:div.select
         (comps/chord-select (:chord evolution))]]]
      [:div.field
       [:label.label {:for "tempo"} "Tempo"]
       [:div.control
        [:input.input {:type "number" :name "tempo" :value (:tempo evolution) :min "40" :max "240"}]]]
      [:div.control
       [:input.button.is-link {:type "submit" :value "Create"}]]]]))

(defn evolution-list [req evolutions]
  (let [{:keys [type]} (-> req :params)]
    (base-view
      req
      [:div
       [:form {:action "/evolution/search" :method "GET"}
        [:div.field.is-horizontal
         [:div.field-label.is-normal
          [:label.label {:for "type"} "Type"]]
         [:div.field.mr-4
          [:div.control
           [:div.select
            (comps/evolution-type-select type)]]]
         [:div.control
          [:input.button.is-link {:type "submit" :value "Search"}]]
         ]]
       [:table.table
        [:thead
         [:tr
          [:th "Id"]
          [:th "Created"]
          [:th "Updated"]
          [:th "Min Ratings"]
          [:th "Evolve After"]
          [:th "Total Iter."]
          [:th "Pop Size"]
          [:th "Crossover"]
          [:th "Mutation"]
          [:th "Key"]
          [:th "Mode"]
          [:th "Progression"]
          [:th "Repetitions"]
          [:th "Chord"]
          [:th "Tempo"]
          [:th "User"]]]
        [:tbody
         (for [e evolutions]
           [:tr
            [:td [:a {:href (str "/evolution/" (:evolution_id e))} (:evolution_id e)]]
            [:td (:created_at e)]
            [:td (:updated_at e)]
            [:td (:min_ratings e)]
            [:td (:evolve_after e)]
            [:td (:total_iterations e)]
            [:td (:population_size e)]
            [:td (:crossover_rate e)]
            [:td (:mutation_rate e)]
            [:td (:key e)]
            [:td (:mode e)]
            [:td (:progression e)]
            [:td (:repetitions e)]
            [:td (:chord e)]
            [:td (:tempo e)]
            [:td (:user_id e)]])]]])))                      ;; TODO admin only

(defn evolution-detail [req {:keys [user-id evolution chromosomes reaction-map pagination
                                    iteration-ratings iteration]}]
  (let [ratings-satisfied? (>= (count iteration-ratings) (:min_ratings evolution))
        should-evolve?     (> (System/currentTimeMillis) (-> iteration :evolve_after (.getTime)))
        finished?          (= (:num iteration) (:total_iterations evolution))
        last?              (:last iteration)]
    (base-view
      req
      [:div
       [:h2.is-size-3.mb-4 (str "Evolution #" (:id evolution))]
       [:h3.is-size-4.mb-4 "Evolution details"]
       [:div
        [:p (str "Key: " (:key evolution))]
        [:hr]
        ]
       [:h3.is-size-4.mb-4 "Iteration details"]
       [:div
        [:p (str "Ratings: " (count iteration-ratings) "/" (:min_ratings evolution))]
        [:p (str "Iteration: " (:num iteration) "/" (:total_iterations evolution))]
        (if last?
          [:p (str "Status: "
                   (cond
                     finished?
                     "Finished"
                     (and ratings-satisfied? should-evolve?)
                     "Should evolve at any moment now"
                     (and should-evolve?)
                     "Not enough ratings to evolve to next iteration"
                     :else
                     (str "Will evolve " (h/datetime (:evolve_after iteration)) (when (not ratings-satisfied?)
                                                                                  " (if will have enough ratings)"))
                     ))]
          [:p [:a {:href (str "/evolution/" (:id evolution))} "Jump to last iteration"]])
        [:hr]
        ]
       (when (= user-id (:user_id evolution))
         [:div
          [:h3.is-size-4.mb-4 "Collaboration"]
          ;; TODO details
          ;; TODO!! disable rating past tracks
          [:p.mb-4 "This is public evolution, people can see it and rate the chromosomes/tracks"]
          [:div.mb-4
           [:a.button.is-primary {:href (u/url-for :invitation-form {:evolution-id (:id evolution)})} "Invite"]]
          [:hr]])
       [:h3.is-size-4.mb-4 "Chromosomes"]
       [:div
        (for [c chromosomes]
          (let [reaction (-> c :chromosome_id reaction-map)]
            (comps/abc-track c :evolution-id (:id evolution) :reaction reaction :user-id user-id)))]
       [:div
        (comps/pagination pagination)]]
      :enable-abc? true
      :body-load-hook "load()"
      )))

(defn first-not-null [errors]
  (ffirst (filter some? errors)))

(defn invitation-form [req {:keys [evolution errors emails notification] :as content}]
  (base-view
    req
    [:div
     [:h3.title.is-4 "Invitation"]
     [:form {:method "post" :action (u/url-for :invitation-save {:evolution-id (:id evolution)})}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:input {:type "hidden" :name "evolution_id" :value (:id evolution)}]
      [:div.field
       [:label.label {:for "emails"} "Emails"]
       [:div.control
        [:input.input {:type "input" :name "emails" :value emails}]]
       (when-let [email-errors (:emails errors)]
         [:p.help.is-danger (first-not-null email-errors)])]
      [:div.control
       [:input.button.is-link {:type "submit" :value "Invite"}]]]]
    :notification notification))
