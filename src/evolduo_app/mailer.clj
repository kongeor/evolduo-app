(ns evolduo-app.mailer
  (:require [postal.core :as postal]
            [evolduo-app.mailjet :as mailjet]
            [evolduo-app.model.user :as user]
            [evolduo-app.model.mail :as mail]
            [hiccup.core :as html]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [clojure.tools.logging :as log]))

;; common

(defn- p [text]
  [:p {:style "font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;"} text])

(defn- a [{:keys [href text]}]
  [:table.btn.btn-primary {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; box-sizing: border-box; width: 100%;" :width "100%"}
   [:tbody
    [:tr
     [:td {:align "left" :style "font-family: sans-serif; font-size: 14px; vertical-align: top; padding-bottom: 15px;" :valign "top"}
      [:table {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: auto;"}
       [:tbody
        [:tr
         [:td {:style "font-family: sans-serif; font-size: 14px; vertical-align: top; border-radius: 5px; text-align: center; background-color: #3498db;" :valign "top" :align "center" :bgcolor "#3498db"}
          [:a {:href href :target "_blank" :style "border: solid 1px #3498db; border-radius: 5px; box-sizing: border-box; cursor: pointer; display: inline-block; font-size: 14px; font-weight: bold; margin: 0; padding: 12px 25px; text-decoration: none; text-transform: capitalize; background-color: #3498db; border-color: #3498db; color: #ffffff;"} text]]]]]]]]])

(defn- verification-url [app-url token]
  (str app-url "/user/verify?token=" token))

(defn- unsubscribe-url [app-url token]
  (str app-url "/user/unsubscribe?token=" token))

(defn evolution-url [app-url evolution-id]
  (str app-url "/evolution/" evolution-id))


(defn email-template [{:keys [app-url company-address] :as settings} {:keys [unsubscribe_token] :as user} {:keys [title content]}]
  [:html
   [:head
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:title title]
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
              (apply list content)
              ]]]]]]
        [:div.footer {:style "clear: both; margin-top: 10px; text-align: center; width: 100%;"}
         [:table {:role "presentation" :border "0" :cellpadding "0" :cellspacing "0" :style "border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" :width "100%"}
          [:tr
           [:td.content-block {:style "font-family: sans-serif; vertical-align: top; padding-bottom: 10px; padding-top: 10px; color: #999999; font-size: 12px; text-align: center;" :valign "top" :align "center"}
            [:span.apple-link {:style "color: #999999; font-size: 12px; text-align: center;"} company-address]
            [:br] "Don't like these emails? " [:a {:href (unsubscribe-url app-url unsubscribe_token) :style "text-decoration: underline; color: #999999; font-size: 12px; text-align: center;"} "Unsubscribe"] "."]]]]]]
      [:td {:style "font-family: sans-serif; font-size: 14px; vertical-align: top;" :valign "top"} "&nbsp;"]]]]])

(comment
  (html/html [:p
              (apply list
                [
                 [:p "foo"]
                 [:p "foo"]])]))

(comment
  (html/html (apply list [(p "foo")
                          (p "bar")])))
;;

(def mailjet-types #{"signup" "invitation"})

(defn- send-email [settings type user subject content]
  (let [mail-server (:mail-server settings)
        {:keys [id email]} user
        html-content (html/html content)]
    (or
      (when (mailjet-types type)
        (log/info "Sending email using mailjet to user" id "with subject" subject)
        (mailjet/send-email settings email subject html-content))
      (do
        (log/info "Sending email using postal to user" id "with subject" subject)
        (postal/send-message mail-server
          {:from    (:user mail-server)
           :to      email
           :subject subject
           :body    [{:type    "text/html"
                      :content html-content}]})))))

(defn- invitation-content [db {:keys [app-url] :as settings} mail]
  (let [{:keys [evolution-id invited-by-id]} (:data mail)
        invited-by-email (:email (user/find-user-by-id db invited-by-id))]
    [(p (str "User " invited-by-email " invited you to collaborate on the following track"))
     (a {:href (evolution-url app-url evolution-id) :text "View"})]))

(defn- get-email-data [db {:keys [app-url] :as settings} {:keys [verification_token subscription] :as user} mail]
  (condp = (:type mail)
    "signup" {:should-receive? true :title "Welcome to Evolduo"
              :content         (email-template settings user {:title "Welcome to Evolduo"
                                                              :content [(p "Welcome to Evolduo")
                                                                      (p "Please click the following link to verify your account")
                                                                      (a {:href (verification-url app-url verification_token) :text "Verify"})]})}
    "invitation" {:should-receive? (:notifications subscription)
                  :title "Invitation to collaborate" ;; TODO should receive, duplication
                  :content         (email-template settings user {:title "Invitation to collaborate"
                                                                  :content (invitation-content db settings mail)})}))

(defn send-mails [db settings]
  (doseq [mail (mail/find-unsent-mails db)]
    (jdbc/with-transaction [tx db]
      (let [tx-opts (jdbc/with-options tx {:builder-fn rs/as-unqualified-lower-maps})
            user (user/find-user-by-id tx-opts (:recipient_id mail))
            {:keys [should-receive? title content]} (get-email-data db settings user mail)]
        (when should-receive?
          (send-email settings (:type mail) user title content))
        (mail/mark-as-sent tx-opts (:id mail))))))

(comment
  (let [settings (:config/settings integrant.repl.state/system)
        db (:database.sql/connection integrant.repl.state/system)
        user (user/find-user-by-email db "foo@example.com")]
    (send-collaboration-email settings 1 "foo@example.com" ["bar@example.com"])))

