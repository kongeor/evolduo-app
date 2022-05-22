(ns evolduo-app.controllers.reaction
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as resp]
            [evolduo-app.model.reaction :as model]
            [evolduo-app.model.evolution :as evolution-model]
            [evolduo-app.schemas :as schemas]
            [next.jdbc :as jdbc])
  (:import (java.time Instant)))

(defn save
  [req]
  (let [user-id (-> req :session :user/id)
        db (:db req)
        data (-> req :params (select-keys [:chromosome_id
                                           :type
                                           :value
                                           ]))
        sanitized-data (schemas/decode-and-validate-reaction data)
        {:keys [iteration_id]} (evolution-model/find-chromosome-by-id db (:chromosome_id data))
        ]
    (log/info "sanitized" sanitized-data)
    ;; TODO is user verified?
    (cond
      (:error sanitized-data)
      (assoc
        (resp/redirect "/evolution/list")
        :flash {:type :danger :message "oops"})

      :else
      (let [reaction (merge (:data sanitized-data)
                       {:created_at   (Instant/now)
                        :iteration_id iteration_id
                        :user_id      user-id})]
        (jdbc/with-transaction [tx db]
          ;; TODO is this ok? as in atomic etc.
          (let [iteration (evolution-model/find-iteration-by-id db iteration_id)]
            (evolution-model/increase-iteration-ratings db iteration)
            (model/insert-reaction tx reaction)))
        (assoc
          (resp/redirect "/evolution/list")
          :flash {:type :info :message "Thanks! Your rating has been recorded"})))))
