(ns evolduo-app.views.playground
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]
            [clojure.string :as str]))

(defn playground [req & {:keys [abc title] :as track}]
  (let [{:keys [key mode progression chord tempo]} (-> req :params)
        tempo (if (str/blank? tempo) "110" tempo)]          ;; TODO sanitized params
    (base-view
      req
      [:div
       [:h3.title.is-3 "Playground"]
       [:p.mb-4 "Playground allows you to try different combinations of keys, modes, progressions and chord types
                 without the result being persisted and evolved."]
       [:form {:action "/playground" :method "GET"}
        [:div.field.is-horizontal
         [:div.field-label.is-normal
          [:label.label {:for "key"} "Key"]]
         [:div.field.mr-4
          [:div.control
           [:div.select
            (comps/keys-select key)]]]
         [:div.field-label.is-normal
          [:label.label {:for "mode"} "Mode"]]
         [:div.field.mr-4
          [:div.control
           [:div.select
            (comps/mode-select mode)]]]
         [:div.field-label.is-normal
          [:label.label {:for "progression"} "Progression"]]
         [:div.field.mr-4
          [:div.control
           [:div.select
            (comps/progression-select progression)]]]
         [:div.field-label.is-normal
          [:label.label {:for "chord"} "Chord"]]
         [:div.field.mr-4
          [:div.control
           [:div.select
            (comps/chord-select chord)]]]
         [:div.field-label.is-normal
          [:label.label {:for "tempo"} "Tempo"]]
         [:div.field.mr-2
          [:div.control
           [:input.input {:type "number" :name "tempo" :value tempo :min "40" :max "240"}]]]
         [:div.control
          [:input.button.is-link {:type "submit" :value "Try"}]]
         ]]
       (when (some? abc)
         [:div
          (comps/abc-track {:chromosome_id 1 :abc abc } :hide-reaction? true)
          #_(comps/abc-track {:chromosome_id 2 :abc abc})])
       ]
      :enable-abc? (some? abc)
      ; :custom-script (str "var abc = \"" abc "\";")
      :body-load-hook "load()"                              ;; TODO fix
      :title title
      )))