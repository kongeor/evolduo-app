(ns evolduo-app.controllers.stats
  (:require [evolduo-app.response :as r]
            [evolduo-app.views.stats :as view]))

(defn stats
  [req]
  (r/render-html view/stats req))
