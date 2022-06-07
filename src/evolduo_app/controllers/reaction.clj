(ns evolduo-app.controllers.reaction
  (:require [clojure.tools.logging :as log]
            [evolduo-app.model.evolution :as evolution-model]
            [evolduo-app.model.rating :as model]
            [evolduo-app.schemas :as schemas]
            [next.jdbc :as jdbc]
            [ring.util.response :as resp]))

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
      (assoc
        (resp/redirect "/evolution/search")
        :flash {:type :danger :message "Ooops, this shouldn't have happened."})

      :else
      (let [reaction (merge (:data sanitized-data)
                       {:iteration_id iteration_id
                        :user_id      user-id})]
        (jdbc/with-transaction [tx db]
          ;; TODO is this ok? as in atomic etc.
          (evolution-model/increase-iteration-ratings tx iteration_id)
          (model/insert-rating tx reaction))
        (assoc
          (resp/redirect redirect-url)
          :flash {:type :info :message "Thanks! Your rating has been recorded"})))))
