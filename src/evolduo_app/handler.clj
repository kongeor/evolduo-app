(ns evolduo-app.handler
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes GET POST ANY]]
            [clojure.tools.logging :as log]
            [evolduo-app.controllers.user2 :as user2-ctl]
            [evolduo-app.controllers.home :as home-ctl]
            [evolduo-app.controllers.evolution :as evolution-ctl]
            [evolduo-app.controllers.explorer :as explorer-ctl]
            [evolduo-app.controllers.reaction :as reaction-ctl]
            [evolduo-app.controllers.invitation :as invitation-ctl]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [sentry-clj.core :as sentry]
            [evolduo-app.music :as music]))

(defn- update-action-seed [existing-seed]
  (println "see>" existing-seed)
  (if existing-seed
    existing-seed
    (music/generate-action-seed)))

(defn wrap-action-seed [handler]
  (fn [req]
    (let [session (:session req)
          action-seed (:action-seed session)]
      (if action-seed
        (handler req)
        (->
          (handler req)
          (assoc :session (assoc session :action-seed (music/generate-action-seed))))))))

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
  (GET "/" [] home-ctl/home)
  (GET "/user/signup" [] user2-ctl/signup-form)
  (POST "/user/signup" [] user2-ctl/signup)
  (GET "/user/login" [] user2-ctl/login-form)
  (POST "/user/login" [] user2-ctl/login)
  (POST "/user/logout" [] user2-ctl/logout-user)
  (GET "/user/account" [] user2-ctl/account)
  (GET "/user/verify" [] user2-ctl/verify-user)
  (GET "/evolution/form" [] evolution-ctl/edit)
  (POST "/evolution/save" [] evolution-ctl/save)
  (GET "/evolution/list" [] evolution-ctl/list)
  (GET "/evolution/:id{[0-9]+}" [] evolution-ctl/detail)
  (GET "/evolution/:id{[0-9]+}/invitation/form" [] invitation-ctl/invitation-form)
  (POST "/evolution/:id{[0-9]+}/invitation/save" [] invitation-ctl/invitation-save)
  (GET "/evolution/:evolution-id{[0-9]+}/iteration/:iteration-id{[0-9]+}" [] evolution-ctl/iteration-detail)
  (GET "/explorer" [] explorer-ctl/explorer)
  (POST "/reaction" [] reaction-ctl/save)
  (route/not-found "404"))

(defn app [db settings]
  (-> routes
    wrap-action-seed
    (wrap-db db)
    (wrap-defaults site-defaults)
    wrap-exception                                          ;; TODO why?!
    (wrap-settings settings)))
