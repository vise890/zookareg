(ns zookareg.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [me.raynes.fs :as fs]
            [zookareg.state :as state]
            [zookareg.utils :as ut])
  (:import java.net.ServerSocket))

(def default-ports
  {:kafka           9999
   :zookeeper       2181
   :schema-registry 8081})

(defn ->ports [zookeeper kafka schema-registry]
  {:kafka           kafka
   :zookeeper       zookeeper
   :schema-registry schema-registry})



(defn ->available-ports []
  (->ports (ut/->available-port)
           (ut/->available-port)
           (ut/->available-port)))

(defn ->zookareg-config [ports]
  {:zookareg.schema-registry/schema-registry {:ports      ports
                                              :_kafka     (ig/ref :zookareg.kafka/kafka)
                                              :_zookeeper (ig/ref :zookareg.zookeeper/zookeeper)}
   :zookareg.kafka/kafka                     {:ports      ports
                                              :_zookeeper (ig/ref :zookareg.zookeeper/zookeeper)}
   :zookareg.zookeeper/zookeeper             {:ports ports}})

(defn halt-zookareg! []
  (when @state/system
    (swap! state/system ig/halt!)))

(defn init-zookareg
  ([] (init-zookareg default-ports))
  ([ports] (let [config    (->zookareg-config ports)
                 config-pp (with-out-str (pprint/pprint config))]
             (log/info "starting ZooKaReg with config:" config-pp)
             (try
               (halt-zookareg!)
               (ig/load-namespaces config)
               (reset! state/system (ig/init config))
               (reset! state/config config)
               (catch clojure.lang.ExceptionInfo ex
                 (ig/halt! (:system (ex-data ex)))
                 (throw (.getCause ex)))))))

(comment
  ;;;
  (init-zookareg)

  (def ports (-> @zookareg.state/config
                 ut/disqualify-keys
                 :kafka
                 :ports))

  ports
  ;;;
)
