(ns evolduo-app.controllers.evolution
  (:require [evolduo-app.model.evolution-manager :as model]
            [ring.util.response :as resp]))

(defn edit
  "Display the add/edit form."
  [req]
  (let [db (:db req)
        evolution (when-let [id (get-in req [:path-params :id])]
                    (model/get-evolution-by-id db id))
        evolution (or evolution
                    {:evolution/min_ratings 1
                     :evolution/tempo 60})
        ]
    (println "evo>" evolution)
    (-> req
      (update :params assoc
        :evolution evolution)
      (assoc :application/view "evolution_form"))))

(defn get-evolutions
  [req]
  (let [evolutions (model/get-evolutions (:db req))]
    (println "****" evolutions)
    (-> req
      (assoc-in [:params :evolutions] evolutions)
      (assoc :application/view "evolution_list"))))

(defn save
  [req]
  #_(-> req
    :params
    (select-keys [:id :first_name :last_name :email :department_id])
    (update :id #(some-> % not-empty Long/parseLong))
    (partial model/save-evolution (:db req)))
  (println "*!*" (-> req :params (select-keys [:id :public :min_ratings
                                               :initial_iterations
                                               :total_iterations
                                               :crossover_rate
                                               :mutation_rate
                                               :key
                                               :pattern
                                               :tempo
                                               :user_id])))
  (let [evolution {:id                 nil
                   :created_at         (java.util.Date.)
                   :public             true
                   :min_ratings        false
                   :initial_iterations 10
                   :total_iterations   20
                   :crossover_rate     30
                   :mutation_rate      5
                   :key                "C"
                   :pattern            "I-IV-V-I"
                   :tempo              80
                   :user_id            1}]
    (model/save-evolution (:db req) evolution))
  (resp/redirect "/evolution/list"))
