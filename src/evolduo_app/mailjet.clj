(ns evolduo-app.mailjet
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log])
  (:import
    (java.net.http HttpClient HttpClient$Version HttpRequest HttpRequest$BodyPublishers HttpResponse$BodyHandlers)
    (java.net URI)
    (java.time Duration)
    (java.util Base64)))

(defn- basic-auth-header [username password]
  (let [v (str username ":" password)]
    (str "Basic " (String. (.encode (Base64/getEncoder) (.getBytes v))))))

(defn send-email [settings email subject content]
  (let [mail-server     (:mail-server settings)
        {:keys [username password]} (:mailjet settings)
        from            (:user mail-server)
        msg             {:Messages [{:From     {:Email from}
                                     :To       [{:Email email}]
                                     :Subject  subject
                                     :HTMLPart content}]}
        msg-json        (json/write-str msg)
        client          (-> (HttpClient/newBuilder)
                          (.version HttpClient$Version/HTTP_1_1)
                          (.build))
        request         (-> (HttpRequest/newBuilder)
                          (.uri (URI/create "https://api.mailjet.com/v3.1/send"))
                          (.timeout (Duration/ofSeconds 5))
                          (.header "Content-Type" "application/json")
                          (.header "Authorization" (basic-auth-header username password))
                          (.POST (HttpRequest$BodyPublishers/ofString msg-json))
                          (.build))
        response        (.send client request (HttpResponse$BodyHandlers/ofString))
        status          (.statusCode response)
        body            (.body response)
        response-data   (json/read-str body :key-fn keyword)
        response-status (some-> response-data :Messages first :Status)]
    (if (and
          (= 200 status)
          (= response-status "success"))
      {:status "ok"}
      (log/warn "Couldn't send email using mailjet" status response-data))))
