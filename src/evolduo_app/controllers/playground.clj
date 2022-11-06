(ns evolduo-app.controllers.playground
  (:require [evolduo-app.music :as music]
            [evolduo-app.response :as r]
            [evolduo-app.views.playground :as view]))

(defn playground
  [req]
  ;; TODO validate
  (let [{:keys [key mode progression chord tempo notes accompaniment]} (-> req :params)
        abc (when (and key progression)
              (let [settings    {:key         key
                                 :mode        mode
                                 :progression progression
                                 :chord       chord
                                 :tempo       tempo
                                 :repetitions 1
                                 :accompaniment accompaniment}
                    chord-names (music/gen-chord-names settings)]
                (music/->abc-track settings
                  {:genes
                   (condp = notes

                     "rests"
                     (music/rest-chromosome settings)

                     "asc"
                     (music/chromatic-chromosome key chord-names :asc? true)

                     "desc"
                     (music/chromatic-chromosome key chord-names :asc? false)

                     (music/random-track settings)
                     )})))]
    (r/render-html view/playground req {:abc           abc
                                        :title "Playground"})))
