(ns evolduo-app.controllers.home
  (:require [evolduo-app.response :as response]
            [evolduo-app.model.evolution-manager :as model]
            [evolduo-app.views.home :as view]
            [evolduo-app.request :as r]))

(defn home
  [req]
  (let [db (:db req)
        user-id (r/user-id req)
        data {:public-evolutions (model/find-active-public-evolutions db user-id)}
        view-f (partial response/render-html view/home req)]
    (cond-> data
      user-id
      (assoc :user-evolutions (model/find-user-active-evolutions db user-id))

      true
      view-f)))

