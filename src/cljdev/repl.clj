(ns cljdev.repl
  "Helpers for managing repl servers."
  (:require
    [clojure.java.io :as io]
    [nrepl.server :as nrepl-server]
    [clojure.core.server :as server]
    [clojure.tools.namespace.repl :as namespace.repl]
    [clojure.repl.deps :as rdep]

    [cljdev.dev]
    [rebel-readline.clojure.main :as rebel-main]
    [rebel-readline.core :as rebel-core]
    [scribe.config :as config]))

; nREPL

(defonce state (atom {:server nil}))

(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

;; pREPL

(def prepl-name "dev")

(defmulti launch (fn [k _] k))

(defmethod launch :portal
  [_key config]
  (let [{:keys [portal] portal-config :portal/config} config]
    (when portal
      (println "aperture: require")
      (rdep/add-lib 'org.endot/aperture {:mvn/version "0.0.1-SNAPSHOT"})
      (println "aperture: opening portal")
      ((requiring-resolve 'aperture.core/open) portal-config)
      (println "aperture: portal launch complete"))))

(defmethod launch :watch-deps
  [_key config]
  (let [{:keys [watch-deps] watch-deps-aliases :watch-deps/aliases} config]
    (when watch-deps
      (println "watch-deps: require")
      (rdep/add-lib 'com.lambdaisland/classpath {:mvn/version "0.6.58"})
      (println "watch-deps: starting")
      ((requiring-resolve 'lambdaisland.classpath.watch-deps/start!) {:aliases watch-deps-aliases})
      (println "watch-deps: done"))))

(defmethod launch :nrepl
  [_key config]
  (let [{:keys [nrepl]} config]
    (when nrepl
      (let [server (nrepl-server/start-server :handler (nrepl-handler))
            port (:port server)]
        (swap! state assoc :server server)
        (spit ".nrepl-port" port)
        (println "nREPL server started on port" port)))))

(defmethod launch :prepl
  [_key config]
  (let [{:keys [prepl]} config]
    (when prepl
      (let [port (+ 5000 (rand-int 1000))]
        (server/start-server {:accept 'clojure.core.server/io-prepl
                              :address "localhost"
                              :port port
                              :name prepl-name})
        (spit ".socket-port" port)
        (println "pREPL server started on port" port)))))

(defmulti shutdown (fn [k _] k))

(defmethod shutdown :default
  [& args]
  ;; Noop
  )

(defmethod shutdown :nrepl
  [_key config]
  (let [{:keys [nrepl]} config]
    (when nrepl
      (let [{:keys [server]} @state]
        (when (not (nil? server))
          (nrepl-server/stop-server server)
          (swap! state assoc :server nil)
          (io/delete-file ".nrepl-port" true))))))

(defmethod shutdown :prepl
  [_key config]
  (let [{:keys [prepl]} config]
    (when prepl
      (server/stop-server prepl-name))))

(def default-config
  {:nrepl true
   :prepl true
   :start-ns 'dev
   :refresh false
   :watch-deps true
   :watch-deps/aliases [:dev :test]
   :portal true
   :portal/config nil})

(defn start
  [config]
  (let [final-config (merge default-config (config/load-config "cljdev") config)
        {:keys [start-ns refresh]} final-config]
    (try (require start-ns) (catch Exception _e))
    (when refresh
      (namespace.repl/refresh))
    (launch :nrepl final-config)
    (launch :prepl final-config)
    (rebel-core/ensure-terminal
      (rebel-main/repl
        :init
        (fn []
          ;; Need to launch these here so that the classloader is
          ;; dynamic and *repl* is true (needed for add-lib)
          (launch :watch-deps final-config)
          (launch :portal final-config)
          (in-ns (if (find-ns start-ns) start-ns 'cljdev.dev)))))
    (shutdown :nrepl final-config)
    (shutdown :prepl final-config)
    (System/exit 0)))

(defn -main [& _args]
  (let [config {:nrepl true :prepl true}]
    (try (require '[dev]) (catch Exception _e))
    (launch :nrepl config)
    (launch :prepl config)
    (rebel-core/ensure-terminal
      (rebel-main/repl :init (fn [] (in-ns (if (find-ns 'dev) 'dev 'cljdev.dev)))))
    (shutdown :nrepl config)
    (shutdown :prepl config)
    (System/exit 0)))

(comment
  (require '[clojure.repl.deps :as rdep])
  (launch :portal {:portal true})
  (rdep/add-lib 'org.endot/aperture {:mvn/version "0.0.1-SNAPSHOT"})
  (rdep/add-lib 'io.github.tonsky/clojure-plus {:mvn/version "1.7.0"})
  (count (clojure.string/split (System/getProperty "java.class.path") #":"))
  (require '[aperture.core :as ap])
  (require '[clojure+.core :as cp.core])
  )
