(ns evolduo-app.handler
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.util.response :as resp]
            [evolduo-app.controllers.user :as user-ctl]
            [evolduo-app.controllers.user2 :as user2-ctl]
            [evolduo-app.controllers.evolution :as evolution-ctl]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]))

(defn my-middleware
  "This middleware runs for every request and can execute before/after logic.

  If the handler returns an HTTP response (like a redirect), we're done.
  Else we use the result of the handler to render an HTML page."
  [handler]
  (fn [req]
    (let [resp (handler req)]
      (if (resp/response? resp)
        resp
        (user-ctl/render-page resp)))))

(def middleware-db
  {:name ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db db)))))})

(defn app [db]
  (ring/ring-handler
    (ring/router
      [["/" {:handler user-ctl/default}]
       ["/reset" {:handler user-ctl/reset-changes}]
       ["/user"
        ["/login" {:post {:handler user2-ctl/login-user-handler}
                   :get {:handler user2-ctl/login}}]]
       ["/evolution"
        ["/form" {:handler evolution-ctl/edit}]
        ["/form/:id" {:get {:parameters {:path {:id int?}}}
                      :handler evolution-ctl/edit}]
        ["/list" {:handler evolution-ctl/get-evolutions}]
        ["/save" {:post {:handler evolution-ctl/save}}]
        ]
       ["/user-old"
        ["/list" {:handler user-ctl/get-users}]
        ["/form" {:handler user-ctl/edit}]
        ["/form/:id" {:get {:parameters {:path {:id int?}}}
                      :handler user-ctl/edit}]
        ["/save" {:post {:handler user-ctl/save}}]
        ["/delete/:id" {:get {:parameters {:path {:id int?}}}
                        :handler user-ctl/delete-by-id}]]]
      {:data {:db db
              :middleware [my-middleware
                           parameters/parameters-middleware
                           wrap-keyword-params
                           middleware-db]}})
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (ring/create-default-handler
        {:not-found (constantly {:status 404 :body "Not found"})}))
    {:middleware [wrap-session]}))                          ;; https://github.com/metosin/reitit/issues/205
