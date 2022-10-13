(ns evolduo-app.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [evolduo-app.controllers.evolution :as evolution-ctl]
            [evolduo-app.controllers.playground :as playground-ctl]
            [evolduo-app.controllers.home :as home-ctl]
            [evolduo-app.controllers.invitation :as invitation-ctl]
            [evolduo-app.controllers.reaction :as reaction-ctl]
            [evolduo-app.controllers.user :as user-ctl]
            [evolduo-app.controllers.static :as static-ctl]
            [evolduo-app.request :as req]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [taoensso.carmine.ring :refer [carmine-store]]
            [sentry-clj.core :as sentry]
            [sentry-clj.ring :as sentry-ring]))

(defn maybe-parse-long [x]
  (if (string? x)
    (parse-long x)
    x))

(defn wrap-settings [handler settings]
  (fn [req]
    (let [user-id (req/user-id req)]
      (handler (assoc req :settings settings
                          :is-admin? (some-> settings :admin-id
                                       maybe-parse-long
                                       (= user-id)))))))

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
  (GET "/evolution/presets" [] evolution-ctl/get-presets)
  (POST "/evolution/presets" [] evolution-ctl/post-presets)
  (GET "/evolution/library" [] evolution-ctl/search)
  (GET "/evolution/:id{[0-9]+}" [] evolution-ctl/detail)
  (GET "/evolution/:id{[0-9]+}/invitation/form" [] invitation-ctl/invitation-form)
  (POST "/evolution/:id{[0-9]+}/invitation/save" [] invitation-ctl/invitation-save)
  (GET "/evolution/:evolution-id{[0-9]+}/iteration/:iteration-num{[0-9]+}" [] evolution-ctl/iteration-detail)
  (GET "/playground" [] playground-ctl/playground)
  (POST "/reaction" [] reaction-ctl/save)
  (GET "/samples" [] static-ctl/samples)
  (GET "/contact" [] static-ctl/contact)
  (GET "/privacy-policy" [] static-ctl/privacy-policy)
  (GET "/terms-of-service" [] static-ctl/terms-of-service)
  (route/not-found "404"))

(comment
  (clojure.string/join (map char (concat (range (int \0) (inc (int \9))) (range (int \a) (int \g)))))
  (random/bytes 16)
  #_(byte-array (mapv byte (seq "a 16-byte secret"))))


(defn app [db settings]
  (-> routes
    (wrap-db db)
    (wrap-settings settings)
    (wrap-defaults (-> site-defaults
                       (assoc-in [:session :cookie-attrs :secure] (= (:environment settings) "prod"))
                       (assoc-in [:session :store] (carmine-store
                                                     {:pool {} :spec {:uri (:redis-uri settings)}}
                                                     {:key-prefix "evolduo:session"}))))
    wrap-exception                                          ;; TODO why?!
    sentry-ring/wrap-sentry-tracing))
