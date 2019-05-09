(ns cljdev.dev
  (:require
    [clojure.java.javadoc :refer [javadoc]]
    [clojure.pprint :refer [pprint]]
    [clojure.reflect :refer [reflect]]
    [clojure.repl :refer [apropos dir doc find-doc pst source]]

    [clojure.tools.namespace.repl :refer [refresh refresh-all clear]]))

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src")
