(ns evolduo-app.views.stats
  (:require [evolduo-app.views.common :refer [base-view]]
            [evolduo-app.model.stats :as model]
            [clojure.data.json :as json]
            [clojure.core.memoize :as memo]))

(def ^:private memo-stats (memo/ttl model/stats :ttl/threshold (* 1000 60 60))) ;; 1h

(defn- fetch-stats [settings db]
  (if (= (:environment settings) "prod")
    (memo-stats db)
    (model/stats db)))

;; TODO only load chart libs when needed

(defn stats [{:keys [settings db] :as req}]
  (let [stats (fetch-stats settings db)]
    (base-view
      req
      [:div
       [:h2.is-size-3.mb-4 "Stats"]
       [:div
        [:canvas.mb-4 {:id "user-stats" :style "max-height: 200px"}]
        [:canvas.mb-4 {:id "rating-stats" :style "max-height: 200px"}]
        [:canvas.mb-4 {:id "evolution-stats" :style "max-height: 200px"}]
        [:canvas.mb-4 {:id "chromosome-stats" :style "max-height: 200px"}]]]
      :custom-script
      (str
        "evolduo = {};
       evolduo.stats = {};
       evolduo.stats.ratings = " (json/write-str (:ratings stats)) ";"
        "evolduo.stats.users =  " (json/write-str (:users stats)) ";"
        "evolduo.stats.chromosomes =  " (json/write-str (:chromosomes stats)) ";"
        "evolduo.stats.evolutions =  " (json/write-str (:evolutions stats)) ";"
        ))))
