(ns evolduo-app.controllers.reaction
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as resp]
            [evolduo-app.model.reaction :as model]
            [evolduo-app.model.evolution-manager :as evolution-model]
            [evolduo-app.schemas :as schemas])
  (:import (java.util Date)))

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
                        {:created_at (Date.)
                         :iteration_id iteration_id
                         :user_id    user-id})]
        (model/insert-reaction db reaction)
        (assoc
          (resp/redirect "/evolution/list")
          :flash {:type :info :message "Thanks! Your rating has been recorded"})))))
