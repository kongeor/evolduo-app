(ns evolduo-app.controllers.invitation
  (:require [evolduo-app.request :as request]
            [evolduo-app.model.evolution-manager :as evolution-model]
            [evolduo-app.model.invitation :as model]
            [evolduo-app.response :as r]
            [evolduo-app.views.evolution :as evolution-views]
            [clojure.string :as str]
            [evolduo-app.schemas :as schemas]
            [evolduo-app.urls :as u]))

(defn invitation-form [req]
  (let [db (:db req)
        user-id (request/user-id req)
        evolution-id (-> req :params :id)
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
        user-id (request/user-id req)
        emails-input (-> req :params :emails)
        emails (str/split emails-input #"[\s,]+")
        sanitized-data (schemas/decode-and-validate-invitation {:emails emails}) ;; TODO don't validate here
        evolution-id (-> req :params :evolution_id)
        evolution (evolution-model/find-evolution-by-id db evolution-id)]
    ;; TODO similar validation?
    ;; have you logged in
    ;; have you verified
    ;; is this yours
    ;; is this a private one? hm?
    ;; how many?
    ;; quota check
    (cond
      (nil? user-id)
      (r/redirect (u/url-for :invitation-form {:evolution-id evolution-id})
        :flash {:type :danger :message "You need to be logged in, man ..."})

      (:error sanitized-data)
      (r/render-html evolution-views/invitation-form req {:evolution evolution
                                                          :errors    (:error sanitized-data)
                                                          :emails    emails-input})

      #_(r/render-html evolution-views/invitation-form req {:evolution evolution}
          :flash {:type :danger :message "You need to be logged in, man ..."}) ;; wrong

      :else
      (let [emails (-> sanitized-data :data :emails)]
        ;; TODO get ids, send emails
        (model/insert-invitations! db user-id evolution-id emails)
        (r/redirect "/" :flash {:type :info :message "Your friends have been invited!"})))))

