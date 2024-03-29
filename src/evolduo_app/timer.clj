(ns evolduo-app.timer
  (:require [chime.core :as c]
            [evolduo-app.model.iteration :as model]
            [evolduo-app.mailer :as mailer]
            [clojure.tools.logging :as log])
  (:import (java.time Instant Duration)))

(defn evolution [db settings]
  (let [now (Instant/now)]
    (c/chime-at
      (c/periodic-seq (Instant/now) (Duration/ofSeconds 20))

      (fn [time]
        (model/evolve-all-iterations db settings))

      {:on-finished (fn []
                      (log/info "Evolution timer finished"))
       :error-handler (fn [e]
                        (log/error e "Oops")
                        true)})))

(defn mail [db settings]
  (let [now (Instant/now)]
    (c/chime-at
      (c/periodic-seq (Instant/now) (Duration/ofSeconds 10))

      (fn [time]
        (mailer/send-mails db settings))

      {:on-finished (fn []
                      (log/info "Mail timer finished"))
       :error-handler (fn [e]
                        (log/error e "Oops")
                        true)})))
