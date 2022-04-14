(ns evolduo-app.controllers.reaction
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as resp]
            [evolduo-app.model.reaction :as model]
            [evolduo-app.schemas :as schemas])
  (:import (java.util Date)))

(defn save
  [req]
  (let [user-id (-> req :session :user/id)
        data (-> req :params (select-keys [:chromosome_id
                                           :type
                                           :value
                                           ]))
        sanitized-data (schemas/decode-and-validate-reaction data)
        ]
    (log/info "sanitized" sanitized-data)
    (cond
      (:error sanitized-data)
      (assoc
        (resp/redirect "/evolution/list")
        :flash {:type :danger :message "oops"})

      :else
      (let [reaction (merge (:data sanitized-data)
                        {:created_at (Date.)
                         :user_id    user-id})]
        (model/insert-reaction (:db req) reaction)
        (assoc
          (resp/redirect "/evolution/list")
          :flash {:type :info :message "Thanks! Your rating has been recorded"})))))
