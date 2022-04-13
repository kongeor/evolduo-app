(ns evolduo-app.mail
  (:require [postal.core :as mail]
            [cprop.core :as cp]
            [evolduo-app.model.user2-manager :as user2]
            [hiccup.core :as html]))

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

(comment
  (let [settings (cp/load-config)
        db (:database.sql/connection integrant.repl.state/system)
        user (user2/find-user-by-email db "foo@example.com")]
    user
    #_(send-welcome-email settings user)))

