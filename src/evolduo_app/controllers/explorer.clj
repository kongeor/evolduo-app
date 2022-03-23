(ns evolduo-app.controllers.explorer
  (:require [evolduo-app.model.evolution-manager :as model]
            [evolduo-app.views.explorer :as explorer-views]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.music :as music]
            [clojure.walk :as walk]
            [ring.util.response :as resp]
            [hiccup.core :as hiccup]
            [clojure.tools.logging :as log]))

(defn explorer
  [req]
  (let [{:keys [key mode pattern]} (-> req :params)
        abc (when (and key pattern)
              (music/progression->abc {:key key :mode (keyword mode) :pattern pattern}))]
    (println abc)
    (-> (resp/response (hiccup/html (explorer-views/explorer req :abc abc)))
      (resp/content-type "text/html"))))
