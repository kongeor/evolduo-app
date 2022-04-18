(ns evolduo-app.timer
  (:require [chime.core :as c]
            [evolduo-app.model.iteration :as model]
            [clojure.tools.logging :as log])
  (:import (java.time Instant Duration)))

(defn start [db settings]
  (let [now (Instant/now)]
    (c/chime-at
      (c/periodic-seq (Instant/now) (Duration/ofMinutes 1))

      (fn [time]
        (model/evolve-all-iterations db settings))

      {:on-finished (fn []
                      (log/info "Schedule finished."))
       :error-handler (fn [e]
                        (log/error e "Oops")
                        true)})))