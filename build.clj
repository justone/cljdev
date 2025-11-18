(ns build
  "Build script.

  clojure -T:build jar
  clojure -T:build deploy

  For more information, run:
  clojure -A:deps -T:build help/doc"
  (:refer-clojure :exclude [test])
  (:require
    [clojure.edn :as edn]
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as dd]))

(def lib 'org.endot/cljdev)
(def version (-> "version.edn" slurp edn/read-string :version))
(def class-dir "target/classes")

(defn- pom-template [version]
  [[:description "A small amount of bootstrap to make starting a Clojure development environment easier."]
   [:url "https://github.com/justone/cljdev"]
   [:licenses
    [:license
     [:name "Eclipse Public License"]
     [:url "https://opensource.org/license/epl-1-0/"]]]
   [:developers
    [:developer
     [:name "Nate Jones"]]]
   [:scm
    [:url "https://github.com/justone/cljdev"]
    [:connection "scm:git:https://github.com/justone/cljdev.git"]
    [:developerConnection "scm:git:ssh:git@github.com:justone/cljdev.git"]
    [:tag (str "v" version)]]])

(defn- base-opts
  [opts]
  (merge opts
         {:basis (b/create-basis {})
          :class-dir class-dir
          :jar-file  (format "target/%s-%s.jar" lib version)
          :lib lib
          :pom-data (pom-template version)
          :version version}))

(defn clean
  "Clean up."
  [_opts]
  (b/delete {:path "target"}))

(defn jar
  "Create jar."
  [opts]
  (let [opts (base-opts opts)]
    (b/write-pom opts)
    (b/copy-dir {:src-dirs ["src"] :target-dir class-dir})
    (b/jar opts)))

(defn- deploy-opts
  [location opts]
  {:installer location
   :artifact (b/resolve-path (:jar-file opts))
   :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))})

(defn deploy
  "Deploy to clojars. Requires CLOJARS_PASSWORD and CLOJARS_USERNAME."
  [opts]
  (dd/deploy (deploy-opts :remote (base-opts opts))))

(defn install
  "Install into local .m2 repository."
  [opts]
  (dd/deploy (deploy-opts :local (base-opts opts))))

(comment
  (clean nil)
  (jar nil)

  (deploy-opts :remote (base-opts nil))
  (deploy-opts :local (base-opts nil))
  )
