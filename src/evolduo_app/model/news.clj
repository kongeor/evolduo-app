(ns evolduo-app.model.news
  (:require [next.jdbc.sql :as sql]
            [clojure.set :as set]
            [markdown.core :as md]
            [hickory.core :as hi]
            [evolduo-app.model.mail :as mail-model]
            [evolduo-app.model.user :as user-model]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.time Instant)))

(defn find-by-id [db id]
  (first (sql/find-by-keys db :news {:id id})))


(defn- -insert-post! [db post]
  (sql/insert! db :news post))

(def ^:private publishing-actions #{"publish" "publish-and-send-emails"})

(defn- prepare-post [user-id post]
  (-> post
      (set/rename-keys {:content :content_md})
      (dissoc :action)
      (assoc :content_html (md/md-to-html-string (:content post))
             :user_id user-id
             :status (if (publishing-actions (:action post)) "published" "draft"))))

(defn- send-post-mails [db action post-id user-id]
  (case action
    "save-and-send-test"
    (mail-model/insert db {:recipient_id user-id
                           :type         "announcement"
                           :data         {:post-id post-id}})
    "publish-and-send-emails"
    (doseq [{:keys [id]} (user-model/find-users-with-enabled-notifications db)]
      (mail-model/insert db {:recipient_id id
                             :type         "announcement"
                             :data         {:post-id post-id}}))
    (do :nothing)))

(defn insert-news! [db user-id {:keys [action] :as post}]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [post' (prepare-post user-id post)
            new-post (-insert-post! tx-opts post')]
        (send-post-mails tx-opts action (:id new-post) user-id)
        new-post))))

(defn update-news! [db user-id post-id {:keys [action] :as post}]
  (jdbc/with-transaction [tx db]
    (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})]
      (let [post' (assoc (prepare-post user-id post)
                    :updated_at (Instant/now))]
        (sql/update! tx-opts :news post' {:id post-id})
        (send-post-mails tx-opts action post-id user-id)
        (find-by-id tx-opts post-id)))))

(defn fetch-news [db filter]
  (sql/find-by-keys db :news filter {:order-by [[:id :desc]]})) ;; TODO ok for now

(defn find-edit-post [db id]
  (let [post (find-by-id db id)]
    (some-> post
            (select-keys [:id :title])
            (assoc :content (:content_md post)))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    #_(find-by-id db 3)
    (sql/find-by-keys db :news :all {:order-by [[:id :asc]]})
    #_(fetch-news db)))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)
        wrapper (partial conj [])]
    (-> (find-by-id db 3)
        :content_md
        md/md-to-html-string
        hi/parse
        hi/as-hiccup
        first
        (nth 3)
        (assoc 0 :div)
        wrapper)
    #_(fetch-news db)))
