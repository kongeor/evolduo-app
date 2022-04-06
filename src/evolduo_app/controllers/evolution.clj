(ns evolduo-app.controllers.evolution
  (:require [evolduo-app.model.evolution-manager :as model]
            [evolduo-app.views.evolution :as evolution-views]
            [evolduo-app.schemas :as schemas]
            [clojure.walk :as walk]
            [ring.util.response :as resp]
            [hiccup.core :as hiccup]
            [clojure.tools.logging :as log]))

(defn edit
  "Display the add/edit form."
  ([req]
   (edit req nil nil))
  ([req evolution errors]
   (let [db (:db req)
         ; evolution (when-let [id (get-in req [:path-params :id])] (model/get-evolution-by-id db id))
         evolution (or evolution
                     {:public             true
                      :min_ratings        2
                      :initial_iterations 10
                      :total_iterations   20
                      :crossover_rate     30
                      :mutation_rate      5
                      :key                "D"
                      :pattern            "I-IV-V-I"
                      :chord              "R + 3 + 3"
                      :tempo              70})]
     (-> (resp/response (hiccup/html (evolution-views/evolution-form req {:evolution evolution
                                                                          :errors errors})))
       (resp/content-type "text/html")))))

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
    (-> (resp/response (hiccup/html (evolution-views/evolution-list req evolutions)))
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
                                           :mode
                                           :pattern
                                           :chord
                                           :tempo]))
        sanitized-data (schemas/decode-and-validate-evolution data)]
    (log/info "sanitized" sanitized-data)
    (cond
      (:error sanitized-data)
      (edit (assoc req :flash {:type :danger :message "oops"}) data (:error sanitized-data))

      :else
      (let [evolution (merge (:data sanitized-data)
                        {:created_at (java.util.Date.)
                         :user_id    user-id})]
        (model/save-evolution (:db req) evolution)
        (assoc
          (resp/redirect "/evolution/list")
          :flash {:type :info :message "Great success!"})))))

(defn render-html [handler req data]
  (-> (resp/response (hiccup/html (handler req data)))
    (resp/content-type "text/html")))

(defn render-404 []
  (-> (resp/not-found (hiccup/html [:h1 "oops"]))
    (resp/content-type "text/html")))

(defn detail
  [req]
  (let [id (-> req :params :id)
        db (:db req)]
    (if-let [evolution (model/get-evolution-by-id db id)]
      (let [iteration-id (model/find-last-iteration-id-for-evolution db (:id evolution))
            chromosomes (model/find-iteration-chromosomes db iteration-id)]
        (render-html evolution-views/evolution-detail req {:evolution evolution
                                                           :chromosomes chromosomes}))
      (render-404))))
