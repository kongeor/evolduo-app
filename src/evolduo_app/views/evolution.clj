(ns evolduo-app.views.evolution
  (:require [evolduo-app.views.common :refer [base-view]]
            [ring.middleware.anti-forgery :as anti-forgery]))

;; TODO move
(def music-keys ["C" "C#" "D"])

(defn- keys-select [evolution]
  [:select {:name "key"}
   (for [k music-keys]
     [:option (merge {:value k}
                (when (= k (:evolution/key evolution))
                  {:selected true})) k])])

(defn evolution-form [evolution]
  (base-view
    [:div
     [:h3.title.is-3 "New Evolution"]
     [:form {:method "post" :action "/evolution/save"}
      [:input {:type "hidden" :id "__anti-forgery-token" :name "__anti-forgery-token" :value anti-forgery/*anti-forgery-token*}]
      [:div.field
       [:div.control
        [:label.checkbox
         [:input (merge {:type "checkbox" :name "public" :value "true"}
                   (when (:evolution/public evolution)
                     {:checked "checked"}))]
         " Public"]]]
      [:div.field
       [:label.label {:for "min_ratings"} "Min Ratings"]
       [:div.control
        [:input.input {:type "number" :name "min_ratings" :value (:evolution/min_ratings evolution) :min "1"}]]]
      [:div.field
       [:label.label {:for "initial_iterations"} "Initial Iterations"]
       [:div.control
        [:input.input {:type "number" :name "initial_iterations" :value (:evolution/initial_iterations evolution) :min "0" :max "20"}]]]
      [:div.field
       [:label.label {:for "total_iterations"} "Total Iterations"]
       [:div.control
        [:input.input {:type "number" :name "total_iterations" :value (:evolution/total_iterations evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "crossover_rate"} "Crossover Rate"]
       [:div.control
        [:input.input {:type "number" :name "crossover_rate" :value (:evolution.evolution/crossover_rate evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "mutation_rate"} "Mutation Rate"]
       [:div.control
        [:input.input {:type "number" :name "mutation_rate" :value (:evolution/mutation_rate evolution) :min "0" :max "100"}]]]
      [:div.field
       [:label.label {:for "key"} "Key"]
       [:div.control
        [:div.select
         (keys-select evolution)]]]
      [:div.field
       [:label.label {:for "pattern"} "Pattern"]
       [:div.control
        [:div.select
         [:select {:name "pattern"}
          [:option {:value "I-IV-V-I"} "I-IV-V-I"]]]]]
      [:div.field
       [:label.label {:for "tempo"} "Tempo"]
       [:div.control
        [:input.input {:type "number" :name "tempo" :value (:evolution/tempo evolution) :min "40" :max "240"}]]]
      [:div.control
       [:input.button.is-link {:type "submit" :value "Create"}]]]]))

(defn evolution-list [evolutions]
  (base-view
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
         [:td (:evolution/id e)]
         [:td (:evolution/created_at e)]
         [:td (:evolution/public e)]
         [:td (:evolution/min_ratings e)]
         [:td (:evolution/initial_iterations e)]
         [:td (:evolution/crossover_rate e)]
         [:td (:evolution/mutation_rate e)]
         [:td (:evolution/key e)]
         [:td (:evolution/pattern e)]
         [:td (:evolution/tempo e)]
         [:td (:evolution/user_id e)]])]]))