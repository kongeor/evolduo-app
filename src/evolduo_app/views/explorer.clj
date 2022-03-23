(ns evolduo-app.views.explorer
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.views.components :as comps]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn explorer [req & {:keys [abc]}]
  (let [{:keys [key mode pattern]} (-> req :params)]
    (base-view
      req
      [:div
       [:form {:action "/explorer" :method "GET"}
        [:div.field.is-horizontal
         [:div.field-label.is-normal
          [:label.label {:for "key"} "Key"]]
         [:div.field
          [:div.control
           [:div.select
            (comps/keys-select key)]]]
         [:div.field-label.is-normal
          [:label.label {:for "mode"} "Mode"]]
         [:div.field
          [:div.control
           [:div.select
            (comps/mode-select mode)]]]
         [:div.field-label.is-normal
          [:label.label {:for "pattern"} "Pattern"]]
         [:div.field
          [:div.control
           [:div.select
            [:select {:name "pattern"}
             [:option {:value "I-IV-V-I"} "I-IV-V-I"]]]]]
         [:div.control
          [:input.button.is-link {:type "submit" :value "Create"}]]
         ]]
       [:div#paper]
       (when (some? abc)
         [:div.buttons
          [:button.button.is-primary.activate-audio "Play"]
          [:button.button.is-light.stop-audio "Stop"]
          [:div.suspend-explanation]
          [:div#start-measure]
          [:div#end-measure]
          ])
       ]
      :enable-abc? (some? abc)
      :custom-script (str "var abc = \"" abc "\";")
      :body-load-hook "load()"
      )))