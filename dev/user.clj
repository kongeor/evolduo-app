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

(def rt-config
  {:datastore  (rt-jdbc/sql-database system/db-spec)
   :migrations (rt-jdbc/load-resources "migrations")})

(comment
  (rt-repl/migrate rt-config))

(comment
  (rt-repl/rollback rt-config))