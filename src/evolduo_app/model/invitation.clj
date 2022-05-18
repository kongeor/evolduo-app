(ns evolduo-app.model.invitation
  (:require [next.jdbc :as jdbc]
            [evolduo-app.sql :as sql])
  (:import (java.time Instant)))

(defn insert-invitations! [db user-id evolution-id emails]
  (jdbc/with-transaction [tx db]
    (let [now (Instant/now)
          base-invitation {:created_at now :evolution_id evolution-id :created_by user-id}]
      (doseq [email emails]
        (sql/insert! tx :invitation (assoc base-invitation :email email))))))
