(ns evolduo-app.controllers.reaction
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as evolution-model]
            [evolduo-app.model.rating :as model]
            [evolduo-app.schemas :as schemas]
            [next.jdbc :as jdbc]
            [evolduo-app.response :as res]
            [evolduo-app.model.user :as user-model]))

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
        {:keys [iteration_id]} (evolution-model/find-chromosome-by-id db (:chromosome_id (:data sanitized-data)))
        ]
    (log/info "sanitized" sanitized-data)
    ;; TODO is user verified?
    (cond
      (:error sanitized-data)
      (res/redirect redirect-url
        :flash {:type :danger :message "Ooops, this shouldn't have happened."})

      (not (:verified (user-model/find-user-by-id db user-id)))
      (res/redirect redirect-url
        :flash {:type :danger :message "You need to verify your email"})

      :else
      (let [reaction (merge (:data sanitized-data)
                       {:iteration_id iteration_id
                        :user_id      user-id})]
        (jdbc/with-transaction [tx db]
          ;; TODO is this ok? as in atomic etc.
          (evolution-model/increase-iteration-ratings tx iteration_id)
          (model/insert-rating tx reaction))
        (res/redirect
          redirect-url
          :flash {:type :info :message "Thanks! Your rating has been recorded"})))))
