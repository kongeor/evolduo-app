(ns evolduo-app.controllers.playground
  (:require [evolduo-app.music :as music]
            [evolduo-app.response :as r]
            [evolduo-app.views.playground :as view]))

(defn playground
  [req]
  ;; TODO validate
  (let [{:keys [key mode progression chord tempo]} (-> req :params)
        abc (when (and key progression)
              (let [settings    {:key         key
                                 :mode        mode
                                 :progression progression
                                 :chord       chord
                                 :tempo       tempo
                                 :repetitions 1}
                    chord-names (music/gen-chord-names settings)]
                (music/->abc-track settings
                                   {:genes (music/chromatic-chromosome 72 chord-names :asc? true) #_(music/random-track {:key  key :measures (count chord-names)
                                                                                                                         :mode mode})})))]
    (r/render-html view/playground req {:abc           abc
                                        :title "Playground"})))