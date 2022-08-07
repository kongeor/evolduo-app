(ns evolduo-app.controllers.evolution
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as model]
            [evolduo-app.model.rating :as reaction-model]
            [evolduo-app.model.iteration :as iteration-model]
            [evolduo-app.request :as request]
            [evolduo-app.response :as r]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.views.evolution :as evolution-views]
            [ring.util.response :as resp]
            [evolduo-app.urls :as u]))

(defn edit
  "Display the add/edit form."
  ([req]
   (edit req nil nil))
  ([req evolution errors]
   (let [evolution (or evolution
                     {:public             true
                      :min_ratings        1
                      :initial_iterations 10
                      :total_iterations   10
                      :population_size    10
                      :crossover_rate     50
                      :mutation_rate      50
                      :key                "C"
                      :mode               "major"
                      :progression        "I-IV-V-I"
                      :repetitions        1
                      :chord              "R + 3 + 3"
                      :tempo              100})]
     (r/render-html evolution-views/evolution-form req {:evolution evolution
                                                        :errors errors}))))

(defn search
  [req]
  (let [type    (-> req :params :type)
        db      (:db req)
        user-id (request/user-id req)
        evolutions (condp = type
                     "public"  (model/find-active-public-evolutions db user-id)
                     "invited" (model/find-invited-to-evolutions db user-id)
                     "my"      (model/find-user-active-evolutions db user-id))]
    (r/render-html evolution-views/evolution-list req evolutions)))

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
                                           :progression
                                           :repetitions
                                           :chord
                                           :tempo]))
        sanitized-data (schemas/decode-and-validate schemas/Evolution data)]
    (log/info "sanitized" sanitized-data)
    (cond
      (nil? user-id)
      (r/redirect (u/url-for :evolution-form)
        :flash {:type :danger :message "You need to be logged in"})

      (:error sanitized-data)
      (edit (assoc req :flash {:type :danger :message "oops"}) data (:error sanitized-data))

      :else
      (let [evolution (merge (:data sanitized-data)
                        {:user_id    user-id
                         :rules {:foo true
                                 :bar true}})]
        (let [{:keys [id]} (model/save-evolution (:db req) (:settings req) evolution)]
          (assoc
            (resp/redirect (u/url-for :evolution-detail {:evolution-id id}))
            :flash {:type :info :message "Great success!"}))))))

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
