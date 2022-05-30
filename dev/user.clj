(ns user
  (:require [integrant.repl :as ig-repl]
            [evolduo-app.system :as system]
            [ragtime.jdbc :as rt-jdbc]
            [ragtime.repl :as rt-repl]))

(ig-repl/set-prep! (fn [] system/config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (go)
  (halt)
  (reset)
  (reset-all))

;; ragtime

(comment
  (let [db (:db (:config/settings integrant.repl.state/system))
        rt-config {:datastore  (rt-jdbc/sql-database db)
                   :migrations (rt-jdbc/load-resources "migrations")}]
    (rt-repl/migrate rt-config)))

(comment
  (let [db (:db (:config/settings integrant.repl.state/system))
        rt-config {:datastore  (rt-jdbc/sql-database db)
                   :migrations (rt-jdbc/load-resources "migrations")}]
    (rt-repl/rollback rt-config)))