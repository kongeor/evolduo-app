(ns evolduo-app.mailer
  (:require [postal.core :as postal]
            [evolduo-app.model.user :as user]
            [evolduo-app.model.mail :as mail]
            [hiccup.core :as html]
            [next.jdbc :as jdbc]))

;; common

(defn email-template []
  [:html
   [:head
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:title "Simple Transactional Email"]
    [:style "@media only screen and (max-width: 620px) {
  table.body h1 {
    font-size: 28px !important;
    margin-bottom: 10px !important;
  }

  table.body p,
table.body ul,
table.body ol,
table.body td,
table.body span,
table.body a {
    font-size: 16px !important;
  }

  table.body .wrapper,
table.body .article {
    padding: 10px !important;
  }

  table.body .content {
    padding: 0 !important;
  }

  table.body .container {
    padding: 0 !important;
    width: 100% !important;
  }

  table.body .main {
    border-left-width: 0 !important;
    border-radius: 0 !important;
    border-right-width: 0 !important;
  }

  table.body .btn table {
    width: 100% !important;
  }

  table.body .btn a {
    width: 100% !important;
  }

  table.body .img-responsive {
    height: auto !important;
    max-width: 100% !important;
    width: auto !important;
  }
}
@media all {
  .ExternalClass {
    width: 100%;
  }

  .ExternalClass,
.ExternalClass p,
.ExternalClass span,
.ExternalClass font,
.ExternalClass td,
.ExternalClass div {
    line-height: 100%;
  }

  .apple-link a {
    color: inherit !important;
    font-family: inherit !important;
    font-size: inherit !important;
    font-weight: inherit !important;
    line-height: inherit !important;
    text-decoration: none !important;
  }

  #MessageViewBody a {
    color: inherit;
    text-decoration: none;
    font-size: inherit;
    font-family: inherit;
    font-weight: inherit;
    line-height: inherit;
  }

  .btn-primary table td:hover {
    background-color: #34495e !important;
  }

  .btn-primary a:hover {
    background-color: #34495e !important;
    border-color: #34495e !important;
  }
}"]]
   [:body {:style "background-color: #f6f6f6; font-family: sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;"}
    [:span.preheader {:style "color: transparent; display: none; height: 0; max-height: 0; max-width: 0; opacity: 0; overflow: hidden; mso-hide: all; visibility: hidden; width: 0;"} "This is preheader text. Some clients will show this text as a preview."]
    [:table.body {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #f6f6f6; width: 100%;" :width "100%" :bgcolor "#f6f6f6"}
     [:tr
      [:td {:style "font-family: sans-serif; font-size: 14px; vertical-align: top;" :valign "top"} "&nbsp;"]
      [:td.container {:style "font-family: sans-serif; font-size: 14px; vertical-align: top; display: block; max-width: 580px; padding: 10px; width: 580px; margin: 0 auto;" :width "580" :valign "top"}
       [:div.content {:style "box-sizing: border-box; display: block; margin: 0 auto; max-width: 580px; padding: 10px;"}
        [:table.main {:role "presentation" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background: #ffffff; border-radius: 3px; width: 100%;" :width "100%"}
         [:tr
          [:td.wrapper {:style "font-family: sans-serif; font-size: 14px; vertical-align: top; box-sizing: border-box; padding: 20px;" :valign "top"}
           [:table {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" :width "100%"}
            [:tr
             [:td {:style "font-family: sans-serif; font-size: 14px; vertical-align: top;" :valign "top"}
              [:p {:style "font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;"} "Hi there,"]
              [:p {:style "font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;"} "Sometimes you just want to send a simple HTML email with a simple design and clear call to action. This is it."]
              [:table.btn.btn-primary {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; box-sizing: border-box; width: 100%;" :width "100%"}
               [:tbody
                [:tr
                 [:td {:align "left" :style "font-family: sans-serif; font-size: 14px; vertical-align: top; padding-bottom: 15px;" :valign "top"}
                  [:table {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: auto;"}
                   [:tbody
                    [:tr
                     [:td {:style "font-family: sans-serif; font-size: 14px; vertical-align: top; border-radius: 5px; text-align: center; background-color: #3498db;" :valign "top" :align "center" :bgcolor "#3498db"}
                      [:a {:href "http://htmlemail.io" :target "_blank" :style "border: solid 1px #3498db; border-radius: 5px; box-sizing: border-box; cursor: pointer; display: inline-block; font-size: 14px; font-weight: bold; margin: 0; padding: 12px 25px; text-decoration: none; text-transform: capitalize; background-color: #3498db; border-color: #3498db; color: #ffffff;"} "Call To Action"]]]]]]]]]
              [:p {:style "font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;"} "This is a really simple email template. Its sole purpose is to get the recipient to click the button with no distractions."]
              [:p {:style "font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;"} "Good luck! Hope it works."]]]]]]]
        [:div.footer {:style "clear: both; margin-top: 10px; text-align: center; width: 100%;"}
         [:table {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" :width "100%"}
          [:tr
           [:td.content-block {:style "font-family: sans-serif; vertical-align: top; padding-bottom: 10px; padding-top: 10px; color: #999999; font-size: 12px; text-align: center;" :valign "top" :align "center"}
            [:span.apple-link {:style "color: #999999; font-size: 12px; text-align: center;"} "Company Inc, 3 Abbey Road, San Francisco CA 94102"]
            [:br] "Don't like these emails?" [:a {:href "http://i.imgur.com/CScmqnj.gif" :style "text-decoration: underline; color: #999999; font-size: 12px; text-align: center;"} "Unsubscribe"] "."]]
          [:tr
           [:td.content-block.powered-by {:style "font-family: sans-serif; vertical-align: top; padding-bottom: 10px; padding-top: 10px; color: #999999; font-size: 12px; text-align: center;" :valign "top" :align "center"} "Powered by" [:a {:href "http://htmlemail.io" :style "color: #999999; font-size: 12px; text-align: center; text-decoration: none;"} "HTMLemail"] "."]]]]]]
      [:td {:style "font-family: sans-serif; font-size: 14px; vertical-align: top;" :valign "top"} "&nbsp;"]]]]])

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
    (postal/send-message mail-server
      {:from    (:user mail-server)
       :to      email
       :subject "Welcome to Evolduo"
       :body    [{:type    "text/html"
                  :content (html/html (email-template)) #_(welcome-html verification-url)}]})))

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
      (postal/send-message mail-server
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


(defn send-mails [db settings]
  (doseq [mail (mail/find-unsent-mails db)]
    (jdbc/with-transaction [tx db]
      (let [user (user/find-user-by-id tx (:recipient_id mail))]
        ;; TODO check if should receive
        (condp = (:type mail)
          "signup" (send-welcome-email settings user))
        (mail/mark-as-sent tx (:id mail))))))