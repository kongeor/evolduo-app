(ns evolduo-app.model.invitation
  (:require [evolduo-app.model.user :as user]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [honey.sql :as h]
            [next.jdbc.result-set :as rs]))


(defn find-by-evolution-and-user
  [db evolution-id user-id]
  (sql/find-by-keys db :invitations {:evolution_id  evolution-id
                                     :invited_by_id user-id}))

(defn num-of-invitations-in-last-day [db user-id]
  (let [q-sqlmap {:select [[[:raw "count(*)"] :count]]
                  :from   [[:invitations :i]]
                  :where
                  [:and
                   [:> :i.created_at [:raw ["now() - interval '1 day'"]]]
                   [:= :i.invited_by_id user-id]]}]
    (:count (first (sql/query db (h/format q-sqlmap))))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (num-of-invitations-in-last-day db 1)
    #_(find-by-evolution-and-user db 4 1)))

(defn insert-invitations! [db user-id evolution-id emails]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})
          base-invitation {:evolution_id evolution-id :invited_by_id user-id}
          users (map #(user/create-stub-or-get! tx-opts %) emails)
          user (user/find-user-by-id tx-opts user-id)
          existing-invitations (find-by-evolution-and-user tx-opts evolution-id user-id)
          existing-invitation-ids (->> existing-invitations
                                    (map :invitee_id)
                                    (into #{}))
          user-ids (->> users
                     (map :id)
                     (into #{})
                     (remove #{user-id})
                     (remove existing-invitation-ids))]
      (doseq [id user-ids]
        (sql/insert! tx-opts :mails {:recipient_id id
                               :type         "invitation"
                               :data         {:invited-by-id user-id
                                              :invited-by-email (:email user)
                                              :evolution-id evolution-id}})
        (sql/insert! tx-opts :invitations (assoc base-invitation :invitee_id id))))))
