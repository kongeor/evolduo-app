(ns evolduo-app.controllers.evolution
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as model]
            [evolduo-app.model.rating :as reaction-model]
            [evolduo-app.model.iteration :as iteration-model]
            [evolduo-app.request :as req]
            [evolduo-app.response :as res]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.views.evolution :as evolution-views]
            [ring.util.response :as resp]
            [evolduo-app.urls :as u]
            [ring.util.codec :as codec]))

(defn edit
  "Display the add/edit form."
  ([req]
   (if (req/user-id req)
     (let [evolution (merge
                       model/default-evolution-params
                       (:params req))
           {:keys [data error]} (schemas/decode-and-validate schemas/Evolution evolution)]
       (res/render-html evolution-views/evolution-form req {:evolution data
                                                            :errors    error}))
     (res/redirect "/user/login" :flash {:type :danger :message "You need to be logged to create an evolution"}))))

(defn search
  [req]
  (let [type    (-> req :params :type)
        db      (:db req)
        user-id (req/user-id req)
        evolutions (condp = type
                     "public"  (model/find-active-public-evolutions db user-id :limit 100)
                     "invited" (model/find-invited-to-evolutions db user-id :limit 100)
                     "my"      (model/find-user-active-evolutions db user-id :limit 100))] ;; TODO pagination
    (res/render-html evolution-views/evolution-list req evolutions)))

(defn save
  [req]
  (let [user-id (-> req :session :user/id)
        data (-> req :params (select-keys [:public
                                           :min_ratings
                                           :evolve_after
                                           ; :initial_iterations
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
        data' (assoc data :initial_iterations 0)            ;; temp patch
        sanitized-data (schemas/decode-and-validate schemas/Evolution data')]
    (log/info "sanitized" sanitized-data)
    (cond
      (nil? user-id)
      (res/redirect (u/url-for :evolution-form)
                    :flash {:type :danger :message "You need to be logged in"})

      (:error sanitized-data)
      (res/render-html evolution-views/evolution-form req {:evolution data'
                                                           :errors    (:error sanitized-data)})

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
        user-id (req/user-id req)
        db (:db req)]
    (if-let [evolution (model/find-evolution-by-id db evolution-id)]
      (let [chromosomes (model/find-iteration-chromosomes db evolution-id iteration-num)
            iteration  (iteration-model/find-by-num db evolution-id iteration-num)
            last-iteration-num (model/find-last-iteration-num-for-evolution db evolution-id)
            reactions (reaction-model/find-iteration-ratings-for-user db evolution-id iteration-num user-id)
            reaction-map (update-vals (group-by :chromosome_id reactions) first)
            iteration-ratings (reaction-model/find-iteration-ratings db evolution-id iteration-num)]
        (res/render-html evolution-views/evolution-detail req {:evolution       evolution
                                                             :chromosomes       chromosomes
                                                             :user-id           user-id
                                                             :reaction-map      reaction-map
                                                             :iteration-ratings iteration-ratings
                                                             :iteration         iteration
                                                             :pagination        {:current (:num iteration)
                                                                          :max last-iteration-num
                                                                          :link-fn #(str "/evolution/" evolution-id "/iteration/" %)}}))
      (res/render-404))))

(defn get-presets [req]
  ;; TODO user needs to be logged-in
  (res/render-html evolution-views/presets req {}))

(defn post-presets [req]
  (let [preset (-> req :params :preset)
        params (model/preset->params preset)]
    (res/redirect (str "/evolution/form?" (codec/form-encode params)))))