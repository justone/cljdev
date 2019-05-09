(ns cljdev.repl
  "Helpers for managing an nREPL server."
  (:require
    [clojure.java.io :as io]
    [nrepl.server :as nrepl-server]

    [cljdev.dev]
    [rebel-readline.clojure.main :as rebel-main]
    [rebel-readline.core :as rebel-core]))

(defonce state (atom {:server nil}))

(defn handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

(defn start-server!
  "Start an nREPL server."
  []
  (let [server (nrepl-server/start-server :handler (handler))
        port (:port server)]
    (swap! state assoc :server server)
    (spit ".nrepl-port" port)
    (println "nREPL server started on port" port)))

(defn stop-server! []
  (let [{:keys [server]} @state]
    (when (not (nil? server))
      (nrepl-server/stop-server server)
      (swap! state assoc :server nil)
      (io/delete-file ".nrepl-port" true))))

(defn -main [& args]
  (try (require '[dev]) (catch Exception e))
  (start-server!)
  (rebel-core/ensure-terminal
    (rebel-main/repl :init (fn [] (in-ns (if (find-ns 'dev) 'dev 'cljdev.dev)))))
  (stop-server!)
  (System/exit 0))
