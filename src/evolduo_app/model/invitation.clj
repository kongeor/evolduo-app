(ns evolduo-app.model.invitation
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [evolduo-app.model.user :as user])
  (:import (java.time Instant)))

(defn insert-invitations! [db user-id evolution-id emails]
  (jdbc/with-transaction [tx db]
    (let [base-invitation {:evolution_id evolution-id :invited_by_id user-id}
          users (map #(user/create-stub-or-get! db %) emails)]
      (doseq [{:keys [id]} users]
        ;; TODO add to mail
        (sql/insert! tx :invitations (assoc base-invitation :invitee_id id))))))
