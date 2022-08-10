(ns evolduo-app.model.captcha
  (:import (net.logicsquad.nanocaptcha.audio AudioCaptcha$Builder Sample)
           (net.logicsquad.nanocaptcha.audio.producer VoiceProducer)
           (java.io BufferedInputStream ByteArrayOutputStream)
           (net.logicsquad.nanocaptcha.content ContentProducer)
           (javax.sound.sampled AudioSystem AudioFileFormat$Type)
           (java.util Base64)))

(def voices ["alex" "bruce" "fred" "ralph" "kathy" "vicki" "victoria"])

(defn create-audio-captcha [content]
  (-> (AudioCaptcha$Builder.)
    (.addContent
      (reify ContentProducer
        (getContent [this]
          content)))
    (.addVoice
      (reify VoiceProducer
        (^Sample getVocalization [this ^char c]
          (let [string-number (Character/toString c)
                ; idx (Integer/parseInt string-number)
                ; files (.get voices idx)
                ; n (rand-int (count files))
                ; filename (.get files n)
                voice         (rand-nth voices)
                filename      (str "/sounds/en/numbers/" c "-" voice ".wav")
                bis (BufferedInputStream. (.getResourceAsStream Sample filename))
                s (Sample. bis)]
            s))))
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