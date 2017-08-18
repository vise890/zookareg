(ns zookareg.state
  (:require [clojure.tools.namespace.repl :as repl]))

(repl/disable-reload!)

(def config (atom nil))

(def system (atom nil))
