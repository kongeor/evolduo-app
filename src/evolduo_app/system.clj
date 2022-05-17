(ns evolduo-app.system
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :refer [run-jetty]]
            [evolduo-app.handler :as handler]
            [evolduo-app.model.user-manager :refer [populate]]
            [evolduo-app.timer :as timer]
            [cprop.core :as cp])
  (:import (org.eclipse.jetty.server Server)))

(def db-spec {:dbtype "sqlite" :dbname "evolduo.db"})       ;; TODO move to config

(def config
  {:adapter/jetty {:handler (ig/ref :handler/run-app) :port 3000}
   :handler/run-app {:db (ig/ref :database.sql/connection)
                     :settings (ig/ref :config/settings)}
   :database.sql/connection db-spec
   :config/settings {}
   :chime/timer {:db (ig/ref :database.sql/connection)
                 :settings (ig/ref :config/settings)}})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (-> opts (dissoc handler) (assoc :join? false))))

(defmethod ig/init-key :handler/run-app [_ {:keys [db settings]}]
  (handler/app db settings))

(defmethod ig/init-key :config/settings [_ _]
  ;; TODO validate config
  (cp/load-config))

(defmethod ig/init-key :chime/timer [_ {:keys [db settings]}]
  (timer/start db settings))

(defmethod ig/init-key :database.sql/connection [_ db-spec]
  (let [conn (jdbc/get-datasource db-spec)]
    #_(populate conn (:dbtype db-spec))                     ;; TODO delete
    conn))

(defmethod ig/halt-key! :adapter/jetty [_ ^Server server]
  (.stop server))

(defmethod ig/halt-key! :chime/timer [_ timer]
  (.close timer))

(defn -main []
  (ig/init config))
