(ns evolduo-app.system
  (:require [cprop.core :as cp]
            [evolduo-app.handler :as handler]
            [evolduo-app.pg]                                ;; json data types
            [evolduo-app.timer :as timer]
            [evolduo-app.live :as live]
            [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [next.jdbc.date-time]                           ;; java 8 time support
            [next.jdbc.result-set :as rs]
            [ragtime.jdbc :as rt-jdbc]
            [ragtime.repl :as rt-repl]
            [ring.adapter.jetty :refer [run-jetty]]
            [sentry-clj.core :as sentry])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource PooledDataSource)
           (next.jdbc.default_options DefaultOptions)
           (org.eclipse.jetty.server Server))
  (:gen-class))

(def config
  {:adapter/jetty           {:handler  (ig/ref :handler/run-app)
                             :settings (ig/ref :config/settings)}
   :handler/run-app         {:db       (ig/ref :database.sql/connection)
                             :settings (ig/ref :config/settings)}
   :database.sql/connection {:settings (ig/ref :config/settings)}
   :database.sql/migrations {:settings (ig/ref :config/settings)}
   :config/settings         {}
   :evolution/timer         {:db       (ig/ref :database.sql/connection)
                             :settings (ig/ref :config/settings)}
   :mail/timer              {:db       (ig/ref :database.sql/connection)
                             :settings (ig/ref :config/settings)}})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler settings] :as opts}]
  (run-jetty handler (-> {:port (:port settings)
                          :host (:host settings)} (assoc :join? false))))

(defmethod ig/init-key :handler/run-app [_ {:keys [db settings]}]
  (handler/app db settings))

(defmethod ig/init-key :config/settings [_ _]
  ;; TODO validate config
  (let [config (cp/load-config)]
    (when (= "prod" (:environment config))
      (sentry/init! (:sentry-url config) {:traces-sample-rate 1.0
                                          :release            (:version config)
                                          :environment        (:environment config)}))
    config))

(defmethod ig/init-key :evolution/timer [_ {:keys [db settings]}]
  (timer/evolution db settings))

(defmethod ig/init-key :mail/timer [_ {:keys [db settings]}]
  (timer/mail db settings))

(defmethod ig/init-key :database.sql/connection [_ {:keys [settings]}]
  (let [^PooledDataSource ds (connection/->pool ComboPooledDataSource (:db settings))
        ds-opts              (jdbc/with-options ds {:builder-fn rs/as-unqualified-lower-maps})]
    ;; this code initializes the pool and performs a validation check:
    (.close (jdbc/get-connection ds-opts))
    ;; otherwise that validation check is deferred until the first connection
    ds-opts))

(defmethod ig/init-key :database.sql/migrations [_ {:keys [settings]}]
  (let [db-spec   (:db settings)
        rt-config {:datastore  (rt-jdbc/sql-database db-spec)
                   :migrations (rt-jdbc/load-resources "migrations")}]
    (rt-repl/migrate rt-config)))

(defmethod ig/halt-key! :database.sql/connection [_ ^DefaultOptions ds]
  (.close (:connectable ds)))

(defmethod ig/halt-key! :adapter/jetty [_ ^Server server]
  (.stop server))

(defmethod ig/halt-key! :evolution/timer [_ timer]
  (.close timer))

(defmethod ig/halt-key! :mail/timer [_ timer]
  (.close timer))

(defn -main [& args]
  (if (= "live" (first args))
    (live/connect-and-show-controls!)
    (ig/init config)))
