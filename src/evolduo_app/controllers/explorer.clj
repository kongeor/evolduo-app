(ns evolduo-app.controllers.explorer
  (:require [evolduo-app.model.evolution :as model]
            [evolduo-app.views.explorer :as explorer-views]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.music :as music]
            [clojure.walk :as walk]
            [ring.util.response :as resp]
            [hiccup.core :as hiccup]
            [clojure.tools.logging :as log]))

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
    (println abc)
    (-> (resp/response (hiccup/html (explorer-views/explorer req :abc abc)))
      (resp/content-type "text/html"))))
