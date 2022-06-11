(ns build
  (:require [clojure.string :as str]
    [clojure.tools.build.api :as b]))


(def lib 'evolduo)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def config-example (str class-dir "/config.edn.example"))
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn config-string []
  (str/replace (slurp config-example)
    #":version \"dev\"" (str ":version \"" version "\"")))

(defn print-version [_]
  (println version))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/write-file {:path   (str class-dir "/config.edn")
                 :string (config-string)})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'evolduo-app.system}))