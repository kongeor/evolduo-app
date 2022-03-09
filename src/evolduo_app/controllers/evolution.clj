(ns evolduo-app.controllers.evolution
  (:require [evolduo-app.model.evolution-manager :as model]
            [evolduo-app.views.evolution :as evolution-views]
            [evolduo-app.schemas :as schemas]
            [clojure.walk :as walk]
            [ring.util.response :as resp]
            [hiccup.core :as hiccup]))

(defn edit
  "Display the add/edit form."
  [req]
  (let [db (:db req)
        evolution (when-let [id (get-in req [:path-params :id])]
                    (model/get-evolution-by-id db id))
        evolution (or evolution
                    {:evolution/public true
                     :evolution/min_ratings 2
                     :evolution/initial_iterations 10
                     :evolution/total_iterations 20
                     :evolution/crossover_rate 30
                     :evolution/mutation_rate 5
                     :evolution/key "D"
                     :evolution/pattern "I-IV-V-I"
                     :evolution/tempo 60})]
    (-> (resp/response (hiccup/html (evolution-views/evolution-form evolution)))
      (resp/content-type "text/html"))))

(defn get-evolutions
  [req]
  (let [evolutions (model/get-evolutions (:db req))]
    (println "****" evolutions)
    (-> req
      (assoc-in [:params :evolutions] evolutions)
      (assoc :application/view "evolution_list"))))

(defn list
  [req]
  (let [evolutions (model/get-evolutions (:db req))]
    (-> (resp/response (hiccup/html (evolution-views/evolution-list evolutions)))
      (resp/content-type "text/html"))))

(defn save
  [req]
  (let [user-id (-> req :session :user/id)
        data (-> req :params (select-keys [:public
                                           :min_ratings
                                           :initial_iterations
                                           :total_iterations
                                           :crossover_rate
                                           :mutation_rate
                                           :key
                                           :pattern
                                           :tempo]))
        sanitized-data (:data (schemas/decode-and-validate-evolution data))
        evolution (merge sanitized-data
                    {:created_at (java.util.Date.)
                     :user_id user-id})]
    (model/save-evolution (:db req) evolution)
  (resp/redirect "/evolution/list")))
