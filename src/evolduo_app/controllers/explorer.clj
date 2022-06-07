(ns evolduo-app.controllers.explorer
  (:require [evolduo-app.music :as music]
            [evolduo-app.response :as r]
            [evolduo-app.views.explorer :as explorer-views]))

(defn explorer
  [req]
  ;; TODO validate
  (let [{:keys [key mode pattern chord tempo]} (-> req :params)
        abc (when (and key pattern)
              (let [settings {:key     key :mode (keyword mode)
                              :pattern pattern :chord chord
                              :tempo   tempo}
                    chord-names (music/gen-chord-names settings)]
                (music/->abc-track settings
                  {:genes (music/random-track {:key  key :measures (count chord-names)
                                               :mode (keyword mode)})})))]
    (r/render-html explorer-views/explorer req {:abc abc})))
