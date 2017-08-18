(ns zookareg.zookeeper
  (:require [integrant.core :as ig])
  (:import org.apache.curator.test.TestingServer))

(defn ->zk [port] (TestingServer. port))

(defn halt! [zk]
  (when zk
    (.close zk)))

(defmethod ig/init-key ::zookeeper [_ {:keys [ports]}]
  (->zk (:zookeeper ports)))

(defmethod ig/halt-key! ::zookeeper [_ zk]
  (halt! zk))

