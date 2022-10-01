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
     (when-not (-> req :params :crossover_rate)
       [:div.notification.is-warning [:span "Too many options? Try " [:a {:href "/evolution/presets"} "presets"] "."]])
     [:form {:method "post" :action "/evolution/save"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:div.control
        [:label.checkbox
         [:input (merge {:type "checkbox" :name "public" :value "true"}
                   (when (:public evolution)
                     {:checked "checked"}))]
         " Public"]]
       [:p.help.is-info "If this evolution is public, users will be able to see it and rate the tracks."]]
      [:div.field
       [:label.label {:for "min_ratings"} "Min Ratings"]
       [:div.control
        [:input.slider.has-output.is-info.is-fullwidth {:id    "min-ratings-with-value"
                                                        :type  "range"
                                                        :name  "min_ratings"
                                                        :value (:min_ratings evolution)
                                                        :step  "1"
                                                        :min   "0" :max "20"}]
        [:output {:for "min-ratings-with-value"} (:min_ratings evolution)]]
       [:p.help.is-info "How many ratings an iteration should have to be able to evolve to the next generation."]]
      [:div.field
       [:label.label {:for "evolve_after"} "Evolve After"]
       [:div.control
        [:div.select
         (comps/evolve-after-select (:evolve_after evolution))]]
       [:p.help.is-info "After how much time should an iteration evolve the the next one. If you are collaborating with others consider giving users some time to listen and rate the tracks."]
       (when-let [key-errors (:evolve_after errors)]
         [:p.help.is-danger (first key-errors)])]
      #_[:div.field
       [:label.label {:for "initial_iterations"} "Initial Iterations"]
       [:div.control
        [:input.input {:type "number" :name "initial_iterations" :value (:initial_iterations evolution) :min "0" :max "20"}]]]
      [:div.field
       [:label.label {:for "total_iterations"} "Total Iterations"]
       [:div.control
        [:input.slider.has-output.is-info.is-fullwidth {:id "total-iterations-with-value"
                                                        :type "range"
                                                        :name "total_iterations"
                                                        :value (:total_iterations evolution)
                                                        :step "10"
                                                        :min "10" :max "50"}]
        [:output {:for "total-iterations-with-value"} (:total_iterations evolution)]]
       [:p.help.is-info "The total number of iterations for this evolution."]]
      [:div.field
       [:label.label {:for "population_size"} "Population Size"]
       [:div.control
        [:input.slider.has-output.is-info.is-fullwidth {:id "population-size-with-value"
                                                        :type "range"
                                                        :name "population_size"
                                                        :value (:population_size evolution)
                                                        :step "10"
                                                        :min "10" :max "40"}]
        [:output {:for "population-size-with-value"} (:population_size evolution)]]
       [:p.help.is-info "The number of tracks each iteration will have."]]
      [:div.field
       [:label.label {:for "crossover_rate"} "Crossover Rate"]
       [:div.control
        [:input.slider.has-output.is-info.is-fullwidth {:id    "crossover-rate-with-value"
                                                        :type  "range"
                                                        :name  "crossover_rate"
                                                        :value (:crossover_rate evolution)
                                                        :step  "1"
                                                        :min   "1" :max "100"}]
        [:output {:for "crossover-rate-with-value"} (:crossover_rate evolution)]]
       [:p.help.is-info "The rate (percentage) of how many tracks will be recombined with other tracks on each iteration."]]
      [:div.field
       [:label.label {:for "mutation_rate"} "Mutation Rate"]
       [:div.control
        [:input.slider.has-output.is-info.is-fullwidth {:id    "mutation-rate-with-value"
                                                        :type  "range"
                                                        :name  "mutation_rate"
                                                        :value (:mutation_rate evolution)
                                                        :step  "1"
                                                        :min   "1" :max "100"}]
        [:output {:for "mutation-rate-with-value"} (:mutation_rate evolution)]]
       [:p.help.is-info "The rate (percentage) of how many notes will be modified on each track."]]
      [:div.field
       [:label.label {:for "key"} "Key"]
       [:div.control
        [:div.select
         (comps/keys-select-restricted (:key evolution))]]
       [:p.help.is-info [:span
                         "Key selection is limited for now. Read more about this limitation "
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
        [:input.slider.has-output.is-info.is-fullwidth {:id "repetitions-with-value"
                                                        :type "range"
                                                        :name "repetitions"
                                                        :value (:repetitions evolution)
                                                        :step "1"
                                                        :min "1" :max "4"}]
        [:output {:for "repetitions-with-value"} (:repetitions evolution)]
        #_[:div.select
         (comps/select "repetitions" (:repetitions evolution) s/repetition-options)]]
       [:p.help.is-info "How many times the chord progression should be repeated."]]
      [:div.field
       [:label.label {:for "chord"} "Chord Intervals"]
       [:div.control
        [:div.select
         (comps/chord-select (:chord evolution))]]
       [:p.help.is-info "R = just the root note, R + 5 + R = 5th chord, R + 3 + 3 = triad chord e.g. C, Cm etc.,
       R + 3 + 3 + 3 = quadrant chord e.g. CMaj7"]]
      [:div.field
       [:label.label {:for "tempo"} "Tempo"]
       [:div.control
        [:input.slider.has-output.is-info.is-fullwidth {:id    "tempo-with-value"
                                                        :type  "range"
                                                        :name  "tempo"
                                                        :value (:tempo evolution)
                                                        :step  "10"
                                                        :min   "40" :max "220"}]
        [:output {:for "tempo-with-value"} (:tempo evolution)]]]
      [:div.control
       [:input.button.is-link {:type "submit" :value "Create"}]]]]))

(defn evolution-list [req evolutions]
  (let [{:keys [type]} (-> req :params)
        is-admin? (:is-admin? req)]
    (base-view
      req
      [:div
       [:h3.title.is-3 "Library"]
       [:p.mb-4 "Here you can find all the tracks you, your friends or others have created."]
       [:form {:action "/evolution/library" :method "GET"}
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
          (when is-admin?
            [:th "User"])
          ]]
        [:tbody
         (for [e evolutions]
           [:tr
            [:td [:a {:href (str "/evolution/" (:evolution_id e))} (:evolution_id e)]]
            [:td {:title (:created_at e)} (h/datetime (:created_at e))]
            [:td {:title (:updated_at e)} (h/datetime (:updated_at e))]
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
            (when is-admin?
              [:td (:user_id e)])
            ])]]]
      :title "Library")))

(defn evolution-detail [req {:keys [user-id evolution chromosomes reaction-map pagination
                                    iteration-ratings iteration rateable? not-rateable-msg]}]
  (let [ratings-satisfied? (>= (count iteration-ratings) (:min_ratings evolution))
        should-evolve?     (> (System/currentTimeMillis) (-> iteration :evolve_after (.getTime)))
        finished?          (= (:num iteration) (:total_iterations evolution))
        last?              (:last iteration)]
    (base-view
      req
      [:div
       [:h2.is-size-3.mb-4 (str "Evolution #" (:id evolution))]
       [:div.columns
        [:div.column
         [:h3.is-size-4.mb-4 "Evolution details"]
         [:div
          [:p (str "Key: " (:key evolution))]
          [:p (str "Mode: " (:mode evolution))]
          [:p (str "Progression: " (:progression evolution))]
          [:p (str "Crossover Rate: " (:crossover_rate evolution) "%")]
          [:p (str "Mutation Rate: " (:mutation_rate evolution) "%")]
          ]]
        [:div.column
         [:h3.is-size-4.mb-4 "Iteration details"]
         [:div
          [:p (str "Ratings (Provided/Required): " (count iteration-ratings) "/" (:min_ratings evolution))]
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
          ]]
        ]
       [:hr]
       (when (= user-id (:user_id evolution))
         [:div
          [:h3.is-size-4.mb-4 "Collaboration"]
          ;; TODO details
          ;; TODO!! disable rating past tracks
          [:p.mb-4
           (if (:public evolution)
             (str
               "This is a public evolution, everyone can see it and rate the tracks. "
               "You can still invite users so they can see those evolutions in their collaboration list.")
             (str
               "This is a private evolution, only you and people you invite can see it and rate the tracks. "
               "Use the invitation button below to invite users to collaborate on this evolution."))]
          [:div.mb-4
           [:a.button.is-primary {:href (u/url-for :invitation-form {:evolution-id (:id evolution)})} "Invite"]]
          [:hr]])
       [:h3.is-size-4.mb-4 "Tracks"]
       [:div
        (for [c chromosomes]
          (let [reaction (-> c :chromosome_id reaction-map)]
            (comps/abc-track c :evolution-id (:id evolution)
              :reaction reaction :user-id user-id :is-admin? (:is-admin? req)
              :rateable? rateable? :not-rateable-msg not-rateable-msg
              :iteration-num (:num iteration))))]
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

(defn- presets-form [preset]
  [:form {:method "post" :action "/evolution/presets"}
   [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
   [:input {:type "hidden" :name "preset" :value preset}]
   [:div.control
    [:input.button.is-link {:type "submit" :value "Select"}]]])

(defn- preset-card [title description preset]
  (let [form-id (str preset "-form")]
    [:form {:id form-id :method "post" :action "/evolution/presets"}
     [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
     [:input {:type "hidden" :name "preset" :value preset}]
     [:div.card
      [:div.card-content
       [:div.title title]
       [:div.content description]]
      [:footer.card-footer
       [:a.card-footer-item.has-text-centered.preset-link
        {:href "#" :onclick (str "document.getElementById('" form-id "').submit()")} "Select"]]]]))

(defn presets [req {}]
  (base-view
    req
    [:div
     [:h3.title.is-3 "Presets"]
     [:p.mb-4 "Presets have a predefined set of options that attempt to match common musical styles."]
     [:p.mb-6 "Each selection will prepopulate some of the existing options which you can still adjust
      before creating a new evolution."]
     [:div.columns
      [:div.column
       (preset-card
         "Minimal"
         "Minimal chord progressions, root or 5th chords, common musical modes, with conservative genetic parameters."
         "minimal")]
      [:div.column
       (preset-card
         "Standard"
         "Common chord progressions, triad chords, common musical modes, with relatively balanced genetic parameters."
         "standard")]
      [:div.column
       (preset-card
         "Progressive"
         "Less common chord progressions, quadrant chords, less common musical modes, with more aggressive genetic parameters."
         "progressive")]
      [:div.column
       (preset-card
         "Experimental"
         "Less common chord progressions, quadrant chords, uncommon musical modes, with very aggressive genetic parameters."
         "experimental")]
      ]]))