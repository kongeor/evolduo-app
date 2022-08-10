(ns evolduo-app.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [evolduo-app.controllers.evolution :as evolution-ctl]
            [evolduo-app.controllers.explorer :as explorer-ctl]
            [evolduo-app.controllers.home :as home-ctl]
            [evolduo-app.controllers.invitation :as invitation-ctl]
            [evolduo-app.controllers.reaction :as reaction-ctl]
            [evolduo-app.controllers.user :as user-ctl]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [sentry-clj.core :as sentry]
            [sentry-clj.ring :as sentry-ring]))

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
                               :throwable   e})
           {:status 500
            :body "Oh no! :'("}))))

(defroutes routes
  (GET "/" [] home-ctl/home)
  (GET "/user/signup" [] user-ctl/signup-form)
  (POST "/user/signup" [] user-ctl/signup)
  (GET "/user/login" [] user-ctl/login-form)
  (POST "/user/login" [] user-ctl/login)
  (POST "/user/logout" [] user-ctl/logout-user)
  (GET "/user/account" [] user-ctl/account)
  (GET "/user/verify" [] user-ctl/verify-user)
  (GET "/user/unsubscribe" [] user-ctl/unsubscribe)
  (POST "/user/subscription" [] user-ctl/update-subscription)
  (POST "/user/delete" [] user-ctl/delete)
  (GET "/evolution/form" [] evolution-ctl/edit)
  (POST "/evolution/save" [] evolution-ctl/save)
  (GET "/evolution/search" [] evolution-ctl/search)
  (GET "/evolution/:id{[0-9]+}" [] evolution-ctl/detail)
  (GET "/evolution/:id{[0-9]+}/invitation/form" [] invitation-ctl/invitation-form)
  (POST "/evolution/:id{[0-9]+}/invitation/save" [] invitation-ctl/invitation-save)
  (GET "/evolution/:evolution-id{[0-9]+}/iteration/:iteration-num{[0-9]+}" [] evolution-ctl/iteration-detail)
  (GET "/explorer" [] explorer-ctl/explorer)
  (POST "/reaction" [] reaction-ctl/save)
  (route/not-found "404"))

(defn app [db settings]
  (-> routes
    (wrap-db db)
    (wrap-defaults site-defaults)
    wrap-exception                                          ;; TODO why?!
    (wrap-settings settings)                                ;; TODO secure cookie
    sentry-ring/wrap-sentry-tracing))
