(ns evolduo-app.model.news
  (:require [clojure.string :as str]
            [next.jdbc.sql :as sql]
            [clojure.set :as set]
            [markdown.core :as md]
            [hickory.core :as hi]))

(defn find-by-id [db id]
  (first (sql/find-by-keys db :news {:id id})))


(defn- -insert-post! [db post]
  (sql/insert! db :news post))

(defn- prepare-post [user-id post]
  (-> post
      (set/rename-keys {:content :content_md})
      (dissoc :action)
      (assoc :content_html (:content post)
             :user_id user-id
             :status "draft")))

(defn insert-news! [db user-id post]
  (let [post' (prepare-post user-id post)]
    (-insert-post! db post')))

(defn update-news! [db user-id post-id post]
  (let [post' (prepare-post user-id post)]
    (sql/update! db :news post' {:id post-id})
    (find-by-id db post-id)))

(defn fetch-news [db]
  (sql/find-by-keys db :news :all))

(defn find-edit-post [db id]
  (let [post (find-by-id db id)]
    (some-> post
            (select-keys [:id :title])
            (assoc :content (:content_md post)))))

(comment
  (let [db (:database.sql/connection integrant.repl.state/system)]
    (find-by-id db 3)
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
