(ns evolduo-app.controllers.home
  (:require [evolduo-app.response :as response]
            [evolduo-app.model.evolution-manager :as model]
            [evolduo-app.views.home :as view]))

(defn home
  [req]
  (let [session (:session req)
        db (:db req)
        user-id (:user/id session)]
    (if user-id
      (let [user-evolutions (model/find-user-active-evolutions db user-id)]
        (response/render-html view/home req {:user-evolutions user-evolutions}))
      (response/render-html (fn [& _] [:h1 "please login"]) req {}))))

