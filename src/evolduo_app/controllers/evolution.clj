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
            [ring.util.codec :as codec]
            [evolduo-app.model.user :as user-model]))

(defn edit
  "Display the add/edit form."
  ([req]
   (let [db      (:db req)
         user-id (req/user-id req)]
     (cond
       (not user-id)
       (res/redirect "/user/login"
         :flash {:type :danger :message "You need to be logged to create an evolution"})

       (not (:verified (user-model/find-user-by-id db user-id)))
       (res/redirect "/user/login"
         :flash {:type :danger :message "You need to verify your email"})

       :else
       (let [evolution (merge
                         model/default-evolution-params
                         (:params req))
             {:keys [data error]} (schemas/decode-and-validate schemas/Evolution evolution)]
         (res/render-html evolution-views/evolution-form req {:evolution data
                                                              :errors    error}))))))

(defn search
  [req]
  (let [type       (-> req :params :type)
        db         (:db req)
        user-id    (req/user-id req)
        evolutions (condp = type
                     "public" (model/find-active-public-evolutions db nil :limit 100) ;; TODO meh
                     "friends" (model/find-invited-to-evolutions db user-id :limit 100)
                     "my" (model/find-user-active-evolutions db user-id :limit 100))] ;; TODO pagination
    (res/render-html evolution-views/evolution-list req evolutions)))

(defn save
  [req]
  (let [db             (:db req)
        user-id        (-> req :session :user/id)
        data           (-> req :params (select-keys [:public
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
        data'          (assoc data :initial_iterations 0)   ;; temp patch
        sanitized-data (schemas/decode-and-validate schemas/Evolution data')]
    (log/info "sanitized" sanitized-data)
    (cond
      (nil? user-id)
      (res/redirect (u/url-for :evolution-form)
        :flash {:type :danger :message "You need to be logged in"})

      (not (:verified (user-model/find-user-by-id db user-id)))
      (res/redirect (u/url-for :evolution-form)
        :flash {:type :danger :message "You need to verify your email"})

      (:error sanitized-data)
      (res/render-html evolution-views/evolution-form req {:evolution data'
                                                           :errors    (:error sanitized-data)})

      (> (model/num-of-evolutions-in-last-day db user-id) 30)
      (res/redirect (u/url-for :evolution-form)
        :flash {:type :danger :message "That's too many evolutions for today. Come again later"})


      :else
      (let [evolution (merge (:data sanitized-data)
                        {:user_id user-id
                         :rules   {:foo true
                                   :bar true}})]
        (let [{:keys [id]} (model/save-evolution db (:settings req) evolution)]
          (assoc
            (resp/redirect (u/url-for :evolution-detail {:evolution-id id}))
            :flash {:type :info :message "Great success!"}))))))

(defn detail [req]
  (let [db                 (:db req)
        evolution-id       (parse-long (-> req :params :id))
        last-iteration-num (model/find-last-iteration-num-for-evolution db evolution-id)]
    ;; TODO conditions, conditions
    ;; TODO create util for url concat
    (resp/redirect (str "/evolution/" evolution-id "/iteration/" last-iteration-num))))

(defn- combined-seed [x y z]
  (unchecked-long
    (+
      x
      (bit-shift-left y 16)
      (bit-shift-left z 32))))

(comment
  (combined-seed 1 100 2000))

(defn- deterministic-shuffle
  [^java.util.Collection coll seed]
  (let [al  (java.util.ArrayList. coll)
        rng (java.util.Random. seed)]
    (java.util.Collections/shuffle al rng)
    (clojure.lang.RT/vector (.toArray al))))

(comment
  (deterministic-shuffle (range 10) 1))

;; TODO move to iteration
(defn iteration-detail
  [req]
  (let [evolution-id  (parse-long (-> req :params :evolution-id))
        iteration-num (parse-long (-> req :params :iteration-num))
        user-id       (req/user-id req)
        db            (:db req)
        evolution     (model/find-evolution-by-id db evolution-id)
        iteration     (iteration-model/find-by-num db evolution-id iteration-num)]
    (if (and evolution iteration)
      (let [chromosomes        (model/find-iteration-chromosomes db evolution-id iteration-num)
            chromosomes'       (if (:is-admin? req)
                                 chromosomes
                                 (deterministic-shuffle chromosomes
                                   (combined-seed (or user-id 0)
                                     (:id evolution)
                                     (:id iteration))))
            last-iteration-num (model/find-last-iteration-num-for-evolution db evolution-id)
            reactions          (reaction-model/find-iteration-ratings-for-user db evolution-id iteration-num user-id)
            reaction-map       (update-vals (group-by :chromosome_id reactions) first)
            rateable?          (and (= last-iteration-num (:num iteration))
                                 (not= (:total_iterations evolution) (:num iteration)))
            not-rateable-msg   (when (not rateable?)
                                 (if (not= last-iteration-num (:num iteration))
                                   "Only tracks from the most recent iteration can be rated"
                                   "This evolution has been finished"))]
        (res/render-html evolution-views/evolution-detail req {:evolution        evolution
                                                               :chromosomes      chromosomes'
                                                               :user-id          user-id
                                                               :reaction-map     reaction-map
                                                               :iteration        iteration
                                                               :rateable?        rateable?
                                                               :not-rateable-msg not-rateable-msg
                                                               :pagination       {:current (:num iteration)
                                                                                  :max     last-iteration-num
                                                                                  :total   (:total_iterations evolution)
                                                                                  :link-fn #(str "/evolution/" evolution-id "/iteration/" %)}}))
      (res/render-404))))

(defn get-presets [req]
  ;; TODO user needs to be logged-in
  (res/render-html evolution-views/presets req {}))

(defn post-presets [req]
  (let [preset (-> req :params :preset)
        params (model/preset->params (:is-admin? req) preset)]
    (res/redirect (str "/evolution/form?" (codec/form-encode params)))))