(ns evolduo-app.system
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :refer [run-jetty]]
            [evolduo-app.handler :as handler]
            [evolduo-app.model.user-manager :refer [populate]]
            [sentry-clj.core :as sentry]
            [cprop.core :as cp]))

(def db-spec {:dbtype "sqlite" :dbname "evolduo.db"})

(def config
  {:adapter/jetty {:handler (ig/ref :handler/run-app) :port 3000}
   :handler/run-app {:db (ig/ref :database.sql/connection)}
   :database.sql/connection db-spec})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (-> opts (dissoc handler) (assoc :join? false))))

(defmethod ig/init-key :handler/run-app [_ {:keys [db]}]
  ;; TODO where should be go to?
  (let [settings (cp/load-config)]
    (sentry/init! (:sentry_url settings))
    (handler/app db settings)))

(defmethod ig/init-key :database.sql/connection [_ db-spec]
  (let [conn (jdbc/get-datasource db-spec)]
    #_(populate conn (:dbtype db-spec))                     ;; TODO delete
    conn))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (.stop server))

(defn -main []
  (ig/init config))
