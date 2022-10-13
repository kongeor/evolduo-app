(ns evolduo-app.model.captcha
  (:import (java.io ByteArrayOutputStream)
           (java.util Base64)
           (javax.sound.sampled AudioFileFormat$Type AudioSystem)
           (net.logicsquad.nanocaptcha.audio AudioCaptcha$Builder)
           (net.logicsquad.nanocaptcha.content ContentProducer)))

(defn create-audio-captcha [content]
  (-> (AudioCaptcha$Builder.)
    (.addContent
      (reify ContentProducer
        (getContent [this]
          content)))
    (.build)))

(defn captcha-audio->base64 [content]
  (let [audio (create-audio-captcha content)
        ais   (-> audio
                (.getAudio)
                (.getAudioInputStream))
        baos  (ByteArrayOutputStream.)]
    (AudioSystem/write ais AudioFileFormat$Type/WAVE baos)
    (String. (.encode (Base64/getEncoder) (.toByteArray baos)))))

(comment
  (captcha-audio->base64 "123456"))