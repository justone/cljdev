(ns cljdev.repl
  "Helpers for managing repl servers."
  (:require
    [clojure.java.io :as io]
    [nrepl.server :as nrepl-server]
    [clojure.core.server :as server]

    [cljdev.dev]
    [rebel-readline.clojure.main :as rebel-main]
    [rebel-readline.core :as rebel-core]))

; nREPL

(defonce state (atom {:server nil}))

(defn handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

(defn start-nrepl!
  "Start an nREPL server."
  []
  (let [server (nrepl-server/start-server :handler (handler))
        port (:port server)]
    (swap! state assoc :server server)
    (spit ".nrepl-port" port)
    (println "nREPL server started on port" port)))

(defn stop-nrepl! []
  (let [{:keys [server]} @state]
    (when (not (nil? server))
      (nrepl-server/stop-server server)
      (swap! state assoc :server nil)
      (io/delete-file ".nrepl-port" true))))

;; pREPL

(def prepl-name "dev")

(defn start-prepl!
  []
  (let [port (+ 5000 (rand-int 1000))]
    (server/start-server {:accept 'clojure.core.server/io-prepl
                          :address "localhost"
                          :port port
                          :name prepl-name})
    (spit ".socket-port" port)
    (println "pREPL server started on port" port)))

(defn stop-prepl!
  []
  (server/stop-server prepl-name))

(defn -main [& args]
  (try (require '[dev]) (catch Exception e))
  (start-nrepl!)
  (start-prepl!)
  (rebel-core/ensure-terminal
    (rebel-main/repl :init (fn [] (in-ns (if (find-ns 'dev) 'dev 'cljdev.dev)))))
  (stop-nrepl!)
  (stop-prepl!)
  (System/exit 0))
