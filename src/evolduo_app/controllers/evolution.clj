(ns evolduo-app.controllers.evolution
  (:require [evolduo-app.model.evolution-manager :as model]
            [evolduo-app.views.evolution :as evolution-views]
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
  #_(-> req
    :params
    (select-keys [:id :first_name :last_name :email :department_id])
    (update :id #(some-> % not-empty Long/parseLong))
    (partial model/save-evolution (:db req)))
  (println "params!" (:params req))
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
