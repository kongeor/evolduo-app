(ns evolduo-app.model.invitation
  (:require [next.jdbc :as jdbc]
            [evolduo-app.sql :as sql]
            [evolduo-app.model.user :as user])
  (:import (java.time Instant)))

(defn insert-invitations! [db user-id evolution-id emails]
  (jdbc/with-transaction [tx db]
    (let [now (Instant/now)
          base-invitation {:created_at now :evolution_id evolution-id :invited_by_id user-id}
          users (map #(user/create-stub-or-get! db %) emails)]
      (doseq [{:keys [id]} users]
        ;; TODO add to mail
        (sql/insert! tx :invitation (assoc base-invitation :invitee_id id))))))
