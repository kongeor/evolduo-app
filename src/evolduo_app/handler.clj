(ns evolduo-app.handler
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes GET POST ANY]]
            [clojure.tools.logging :as log]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.util.response :as resp]
            [evolduo-app.controllers.user :as user-ctl]
            [evolduo-app.controllers.user2 :as user2-ctl]
            [evolduo-app.controllers.evolution :as evolution-ctl]
            [evolduo-app.controllers.explorer :as explorer-ctl]
            [evolduo-app.controllers.reaction :as reaction-ctl]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [sentry-clj.core :as sentry]
            [cprop.core :as cp]))

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

(defn wrap-settings [handler settings]
  (fn [req]
    (handler (assoc req :settings settings))))

(defn wrap-db [handler db]
  (fn [req]
    (handler (assoc req :db db))))

(defn wrap-exception [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (log/error e)
           (sentry/send-event {:message     (.getMessage e)
                               :environment (-> request :settings :environment)
                               :version     (-> request :settings :version)
                               :throwable   e})
           {:status 500
            :body "Oh no! :'("}))))

(defroutes routes
  (GET "/" [] user2-ctl/home)
  (GET "/user/login" [] user2-ctl/login)
  (POST "/user/login" [] user2-ctl/login-user-handler)
  (GET "/user/verify" [] user2-ctl/verify-user)
  (GET "/evolution/form" [] evolution-ctl/edit)
  (POST "/evolution/save" [] evolution-ctl/save)
  (GET "/evolution/list" [] evolution-ctl/list)
  (GET "/evolution/:id{[0-9]+}" [] evolution-ctl/detail)
  (GET "/evolution/:evolution-id{[0-9]+}/iteration/:iteration-id{[0-9]+}" [] evolution-ctl/iteration-detail)
  (GET "/explorer" [] explorer-ctl/explorer)
  (POST "/reaction" [] reaction-ctl/save)
  (route/not-found "404"))

(defn app [db settings]
  (-> routes
    (wrap-db db)
    (wrap-defaults site-defaults)
    wrap-exception                                          ;; TODO why?!
    (wrap-settings settings)))

(macroexpand '(-> 1
                inc
                dec))

(defn app' [db]
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
                           wrap-params
                           middleware-db]}})
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (ring/create-default-handler
        {:not-found (constantly {:status 404 :body "Not found"})}))
    {:middleware [wrap-anti-forgery
                  wrap-session ;; https://github.com/metosin/reitit/issues/205
                  ]}))
