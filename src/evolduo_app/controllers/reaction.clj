(ns evolduo-app.controllers.reaction
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as evolution-model]
            [evolduo-app.model.rating :as model]
            [evolduo-app.schemas :as schemas]
            [next.jdbc :as jdbc]
            [evolduo-app.response :as res]
            [evolduo-app.model.user :as user-model]
            [evolduo-app.model.iteration :as iteration-model]
            [evolduo-app.model.invitation :as invitation-model]
            [evolduo-app.urls :as u]
            [evolduo-app.urls :as urls]))

(defn save
  [req]
  (let [user-id (-> req :session :user/id)
        db (:db req)
        data (-> req :params (select-keys [:chromosome_id
                                           :type
                                           :value
                                           ]))
        redirect-url (-> req :params :redirect_url)
        sanitized-data (schemas/decode-and-validate schemas/Rating data)
        ]
    (cond
      (not user-id)
      (res/redirect redirect-url
        :flash {:type :danger :message "Ooops, this shouldn't have happened."})

      (:error sanitized-data)
      (res/redirect redirect-url
        :flash {:type :danger :message "Ooops, this shouldn't have happened."})

      (not (:verified (user-model/find-user-by-id db user-id)))
      (res/redirect redirect-url
        :flash {:type :danger :message "You need to verify your email"})

      (>= (model/num-of-ratings-in-last-day db user-id) 200)
      (res/redirect redirect-url
        :flash {:type :danger :message "That's enough ratings for today. Please try again later."})

      :else
      (let [chromosome-id (:chromosome_id (:data sanitized-data))
            {:keys [iteration_id]} (evolution-model/find-chromosome-by-id db chromosome-id)
            {:keys [num last evolution_id]} (iteration-model/find-by-id db iteration_id)
            evolution (evolution-model/find-evolution-by-id db evolution_id)
            finished?          (= num (:total_iterations evolution))
            reaction (merge (:data sanitized-data)
                       {:iteration_id iteration_id
                        :user_id      user-id})
            chromosome-hash-link (urls/url-for :iteration-detail-with-hash {:evolution-id  evolution_id
                                                                            :iteration-num num
                                                                            :chromosome-id chromosome-id})]

        (cond
          finished?
          (res/redirect redirect-url
            :flash {:type :danger :message "This evolution has been finished"})

          (not last)
          (res/redirect redirect-url
            :flash {:type :danger :message [:span
                                            (str "Ooops, time's up! Click ")
                                            [:a {:href (u/url-for :evolution-detail {:evolution-id evolution_id})} "here"]
                                            " to jump to the most recent iteration."]})

          (not (or
                 (:public evolution)
                 (= user-id (:user_id evolution))
                 (seq (invitation-model/find-by-evolution-and-invitee-id db evolution_id user-id))))
          (res/redirect redirect-url
            :flash {:type :danger :message "You don't have access to this evolution"})

          :else
          (do
            (jdbc/with-transaction [tx db]
              ;; TODO is this ok? as in atomic etc.
              (evolution-model/increase-iteration-ratings tx iteration_id)
              (model/insert-rating tx reaction))
            (res/redirect
              redirect-url
              :flash {:type :info :message [:span
                                            (str "Thanks! Your rating has been recorded. Click ")
                                            [:a {:href chromosome-hash-link} "here"]
                                            " to scroll to the track you rated."]
                      })))))))
