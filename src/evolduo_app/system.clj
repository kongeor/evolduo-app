(ns evolduo-app.system
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :refer [run-jetty]]
            [evolduo-app.handler :as handler]
            [evolduo-app.timer :as timer]
            [evolduo-app.mailer :as mailer]
            [cprop.core :as cp])
  (:import (org.eclipse.jetty.server Server))
  (:gen-class))

(def db-spec {:dbtype "sqlite" :dbname "evolduo.db"})       ;; TODO move to config

(def config
  {:adapter/jetty           {:handler (ig/ref :handler/run-app) :port 3000}
   :handler/run-app         {:db       (ig/ref :database.sql/connection)
                             :settings (ig/ref :config/settings)}
   :database.sql/connection db-spec
   :config/settings         {}
   :evolution/timer         {:db       (ig/ref :database.sql/connection)
                             :settings (ig/ref :config/settings)}
   :mail/timer              {:db       (ig/ref :database.sql/connection)
                             :settings (ig/ref :config/settings)}})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (-> opts (dissoc handler) (assoc :join? false))))

(defmethod ig/init-key :handler/run-app [_ {:keys [db settings]}]
  (handler/app db settings))

(defmethod ig/init-key :config/settings [_ _]
  ;; TODO validate config
  (cp/load-config))

(defmethod ig/init-key :evolution/timer [_ {:keys [db settings]}]
  (timer/evolution db settings))

(defmethod ig/init-key :mail/timer [_ {:keys [db settings]}]
  (mailer/send-mails db settings))

(defmethod ig/init-key :database.sql/connection [_ db-spec]
  (let [conn (jdbc/get-datasource db-spec)]
    conn))

(defmethod ig/halt-key! :adapter/jetty [_ ^Server server]
  (.stop server))

(defmethod ig/halt-key! :evolution/timer [_ timer]
  (.close timer))

(defn -main []
  (ig/init config))
