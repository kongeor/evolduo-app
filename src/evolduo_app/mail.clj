(ns evolduo-app.mail
  (:require [postal.core :as mail]
            [evolduo-app.model.user :as user]
            [hiccup.core :as html]))

;; common

;; TODO add unsubscribe url etc.
(defn footer []
  [:p "Click " [:a {:href "#"} "here"] "to stop receiving messages "])


;;

(defn verification-url [app-url verification-url]
  (str app-url "/user/verify?token=" verification-url))

(defn welcome-html [verification-url]
  (html/html [:div
              [:p "Welcome to Evolduo"]
              [:p "Click "
               [:a {:href verification-url} "here"]
               " to verify your email"]
              ]))

(defn send-welcome-email [settings user]
  (let [mail-server (:mail_server settings)
        app-url (:app_url settings)
        {:keys [email verification_token]} user
        verification-url (verification-url app-url verification_token)]
    (mail/send-message mail-server
      {:from    (:user mail-server)
       :to      email
       :subject "Welcome to Evolduo"
       :body    [{:type    "text/html"
                  :content (welcome-html verification-url)}]})))

(defn evolution-url [app-url evolution-id]
  (str app-url "/evolution/" evolution-id))

(defn collaboration-html [sender-email evolution-url]
  (html/html [:div
              [:p (str sender-email " has invited you to collaborate on")
               [:a {:href evolution-url} " music generation"]]]))


;; TODO insert emails to a table for auditing, spam prevention and quotas
(defn send-collaboration-email [settings evolution-id sender-email emails]
  (let [mail-server (:mail_server settings)
        app-url (:app_url settings)
        ;; TODO create stuff user for notifications? Is this allowed?
        evolution-url (evolution-url app-url evolution-id)]
    (doseq [email emails]
      (mail/send-message mail-server
        {:from    (:user mail-server)
         :to      email
         :subject "Collaboration invitation"
         :body    [{:type    "text/html"
                    :content (collaboration-html sender-email evolution-url)}]}))))

(comment
  (let [settings (:config/settings integrant.repl.state/system)
        db (:database.sql/connection integrant.repl.state/system)
        user (user/find-user-by-email db "foo@example.com")]
    (send-collaboration-email settings 1 "foo@example.com" ["bar@example.com"])))

