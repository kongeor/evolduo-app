(ns evolduo-app.controllers.captcha
  (:require [clojure.java.io :as io]
            [ring.util.response :as r]
            [evolduo-app.model.captcha :as captcha])
  (:import (net.logicsquad.nanocaptcha.audio AudioCaptcha$Builder Sample)
           (net.logicsquad.nanocaptcha.content ContentProducer)
           (net.logicsquad.nanocaptcha.audio.producer VoiceProducer RandomNumberVoiceProducer)
           (java.io BufferedInputStream ByteArrayOutputStream)
           (javax.sound.sampled AudioSystem AudioFileFormat AudioFileFormat$Type)))

#_(BufferedInputStream. (.getResourceAsStream Sample "/sounds/en/numbers/1-fred.wav"))

#_(io/resource "/sounds/en/numbers/1-fred.wav")

(defn audio-captcha
  [req]
  (if-let [captcha (-> req :session :captcha)]
    (let [audio (captcha/create-audio-captcha captcha)
          ais   (-> audio
                  (.getAudio)
                  (.getAudioInputStream))
          baos  (ByteArrayOutputStream.)]
      (AudioSystem/write ais AudioFileFormat$Type/WAVE baos)
      (r/content-type
        (r/response (.toByteArray baos))
        "audio/wav"))
    (r/not-found nil)))

(comment
  (let [clip (AudioSystem/getClip)
        ais  (-> (create-audio-captcha "123456")
               (.getAudio)
               (.getAudioInputStream))
        baos (ByteArrayOutputStream.)
        ]
    (AudioSystem/write ais AudioFileFormat$Type/WAVE baos)
    (.toByteArray baos)
    #_(.open clip ais)
    #_(.start clip)))

