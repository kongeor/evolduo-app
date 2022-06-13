(ns evolduo-app.image
  (:import (java.awt.image BufferedImage)
           (java.awt Font RenderingHints Color)
           (javax.imageio ImageIO)
           (java.io ByteArrayOutputStream)
           (java.util Base64)))

(defn- text-to-bytes [text]
  (let [text   (str text " ")                               ;; hack but quick way around
        img    (BufferedImage. 1 1 BufferedImage/TYPE_INT_ARGB)
        g2d    (.createGraphics img)
        font   (Font. "Arial" Font/PLAIN 26)
        _      (.setFont g2d font)
        fm     (.getFontMetrics g2d)
        width  (.stringWidth fm text)
        height (.getHeight fm)
        _      (.dispose g2d)

        img    (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g2d    (.createGraphics img)
        _      (doto g2d
                 (.setRenderingHint RenderingHints/KEY_ALPHA_INTERPOLATION RenderingHints/VALUE_ALPHA_INTERPOLATION_QUALITY)
                 (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
                 (.setRenderingHint RenderingHints/KEY_COLOR_RENDERING RenderingHints/VALUE_COLOR_RENDER_QUALITY)
                 (.setRenderingHint RenderingHints/KEY_DITHERING RenderingHints/VALUE_DITHER_ENABLE)
                 (.setRenderingHint RenderingHints/KEY_FRACTIONALMETRICS RenderingHints/VALUE_FRACTIONALMETRICS_ON)
                 (.setRenderingHint RenderingHints/KEY_INTERPOLATION RenderingHints/VALUE_INTERPOLATION_BILINEAR)
                 (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY)
                 (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_PURE))

        _      (.setFont g2d font)
        fm     (.getFontMetrics g2d)
        _      (doto g2d
                 (.setColor Color/BLACK)
                 (.drawString text 0 (.getAscent fm))
                 .dispose)

        ; bytes (.toByteArray img)
        baos   (ByteArrayOutputStream.)
        _      (ImageIO/write img "png" baos)
        ]
    (.toByteArray baos)))

(defn text-to-base64 [text]
  (String. (.encode (Base64/getEncoder) (text-to-bytes text))))
