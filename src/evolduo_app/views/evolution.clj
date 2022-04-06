(ns evolduo-app.views.evolution
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]
            [ring.middleware.anti-forgery :as anti-forgery]))

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
        [:input.input {:type "number" :name "min_ratings" :value (:min_ratings evolution) :min "1"}]]]
      [:div.field
       [:label.label {:for "initial_iterations"} "Initial Iterations"]
       [:div.control
        [:input.input {:type "number" :name "initial_iterations" :value (:initial_iterations evolution) :min "0" :max "20"}]]]
      [:div.field
       [:label.label {:for "total_iterations"} "Total Iterations"]
       [:div.control
        [:input.input {:type "number" :name "total_iterations" :value (:total_iterations evolution) :min "0" :max "100"}]]]
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
         (comps/keys-select (:key evolution))]]
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
       [:label.label {:for "pattern"} "Pattern"]
       [:div.control
        [:div.select
         (comps/pattern-select (:pattern evolution))]]]
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
  (base-view
    req
    [:table.table
     [:thead
      [:tr
       [:th "Id"]
       [:th "Created At"]
       [:th "Public"]
       [:th "Min Ratings"]
       [:th "initial Iterations"]
       [:th "Crossover Rate"]
       [:th "Mutation Rate"]
       [:th "Key"]
       [:th "Pattern"]
       [:th "Tempo"]
       [:th "User"]]]
     [:tbody
      (for [e evolutions]
        [:tr
         [:td [:a {:href (str "/evolution/" (:id e))} (:id e)]]
         [:td (:created_at e)]
         [:td (:public e)]
         [:td (:min_ratings e)]
         [:td (:initial_iterations e)]
         [:td (:crossover_rate e)]
         [:td (:mutation_rate e)]
         [:td (:key e)]
         [:td (:pattern e)]
         [:td (:tempo e)]
         [:td (:user_id e)]])]]))

(defn evolution-detail [req {:keys [evolution chromosomes]}]
  (base-view
    req
    [:h2 (str "Evolution #" (:id evolution))
     [:div
      (for [c chromosomes]
        (comps/abc-track c))]]
    :enable-abc? true
    :body-load-hook "load()"
    ))