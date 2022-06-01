(ns evolduo-app.controllers.invitation
  (:require [evolduo-app.request :as request]
            [evolduo-app.model.evolution :as evolution-model]
            [evolduo-app.model.user :as user-model]
            [evolduo-app.model.invitation :as model]
            [evolduo-app.response :as r]
            [evolduo-app.views.evolution :as evolution-views]
            [clojure.string :as str]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.urls :as u]
            [evolduo-app.mailer :as mail]))

(defn invitation-form [req]
  (let [db (:db req)
        user-id (request/user-id req)
        evolution-id (parse-long (-> req :params :id))
        evolution (evolution-model/find-evolution-by-id db evolution-id)]
    ;; have you logged in
    ;; have you verified
    ;; is this yours
    ;; is this a private one? hm?
    ;; quota check
    (case
      #_(not user-id)
      #_(r/render-html evolution-views/invitation-form req {:evolution evolution}
          :flash {:type :danger :message "You need to be logged in, man ..."}) ;; wrong

      :else
      (r/render-html evolution-views/invitation-form req {:evolution evolution}))))

#_(str/split "f@ac.c    as@asdf.co,,,,,zxcv@asdf.co" #"[\s,]+")

(defn invitation-save [req]
  (let [db (:db req)
        settings (:settings req)
        user-id (request/user-id req)
        emails-input (-> req :params :emails)
        emails (str/split emails-input #"[\s,]+")
        sanitized-data (schemas/decode-and-validate-invitation {:emails emails})
        evolution-id (parse-long (-> req :params :evolution_id))
        evolution (evolution-model/find-evolution-by-id db evolution-id)]
    (cond
      (nil? user-id)
      (r/redirect (u/url-for :invitation-form {:evolution-id evolution-id})
        :flash {:type :danger :message "You need to be logged in"})

      (not (:verified (user-model/find-user-by-id db user-id)))
      (r/render-html evolution-views/invitation-form req {:evolution evolution
                                                          :notification {:type "danger"
                                                                         :message "You need to verify your email"}
                                                          :emails    emails-input})

      (not (evolution-model/find-evolution-by-id-and-user-id db evolution-id user-id))
      (r/render-html evolution-views/invitation-form req {:evolution evolution
                                                          :notification {:type "danger"
                                                                         :message "Oops!"}
                                                          :emails    emails-input})

      (:error sanitized-data)
      (r/render-html evolution-views/invitation-form req {:evolution evolution
                                                          :errors    (:error sanitized-data)
                                                          :emails    emails-input})

      (> (count emails) 5)
      (r/render-html evolution-views/invitation-form req {:evolution evolution
                                                          :errors    {:emails [["You can invite up to 5 friends at a time"]]}
                                                          :emails    emails-input})

      (> (model/num-of-invitations-in-last-day db user-id) 30)
      (r/render-html evolution-views/invitation-form req {:evolution evolution
                                                          :notification {:type "danger"
                                                                         :message "That's too many invitations for today. Come again later"}
                                                          :emails    emails-input})

      :else
      (let [user (user-model/find-user-by-id db user-id)
            emails (-> sanitized-data :data :emails)]
        (model/insert-invitations! db user-id evolution-id emails)
        (r/redirect "/" :flash {:type :info :message "Your friends have been invited!"})))))

;; TODO store cookie data