(ns evolduo-app.controllers.evolution
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as model]
            [evolduo-app.model.rating :as reaction-model]
            [evolduo-app.model.iteration :as iteration-model]
            [evolduo-app.request :as request]
            [evolduo-app.response :as r]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.views.evolution :as evolution-views]
            [hiccup.core :as hiccup]
            [ring.util.response :as resp]))

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
                      :population_size    10
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

(defn search
  [req]
  (let [evolutions (model/get-evolutions (:db req))]
    (-> (resp/response (hiccup/html (evolution-views/evolution-list req evolutions)))
      (resp/content-type "text/html"))))

(defn save
  [req]
  (let [user-id (-> req :session :user/id)
        data (-> req :params (select-keys [:public
                                           :min_ratings
                                           :evolve_after
                                           :initial_iterations
                                           :total_iterations
                                           :population_size
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
                        {:user_id    user-id
                         :rules {:foo true
                                 :bar true}})]
        (model/save-evolution (:db req) (:settings req) evolution)
        (assoc
          (resp/redirect "/evolution/list")
          :flash {:type :info :message "Great success!"})))))

(defn detail [req]
  (let [db (:db req)
        evolution-id (parse-long (-> req :params :id))
        last-iteration-num (model/find-last-iteration-num-for-evolution db evolution-id)]
    ;; TODO conditions, conditions
    ;; TODO create util for url concat
    (resp/redirect (str "/evolution/" evolution-id "/iteration/" last-iteration-num))))

;; TODO move to iteration
(defn iteration-detail
  [req]
  (let [evolution-id (parse-long (-> req :params :evolution-id))
        iteration-num (parse-long (-> req :params :iteration-num))
        user-id (request/user-id req)
        db (:db req)]
    (if-let [evolution (model/find-evolution-by-id db evolution-id)]
      (let [chromosomes (model/find-iteration-chromosomes db evolution-id iteration-num)
            iteration  (iteration-model/find-by-num db iteration-num)
            last-iteration-num (model/find-last-iteration-num-for-evolution db evolution-id)
            reactions (reaction-model/find-iteration-ratings-for-user db evolution-id iteration-num user-id)
            reaction-map (update-vals (group-by :chromosome_id reactions) first)]
        (r/render-html evolution-views/evolution-detail req {:evolution evolution
                                                             :chromosomes chromosomes
                                                             :user-id user-id
                                                             :reaction-map reaction-map
                                                             :pagination {:current (:num iteration)
                                                                          :max last-iteration-num
                                                                          :link-fn #(str "/evolution/" evolution-id "/iteration/" %)}}))
      (r/render-404))))
