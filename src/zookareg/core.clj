(ns zookareg.core
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [me.raynes.fs :as fs]
            [zookareg.state :as state]
            [zookareg.utils :as ut])
  (:import java.net.ServerSocket))

;; TODO use vanilla kafka

(defn ->zookareg-config []
  (let [ports {:kafka           (ut/->available-port)
               :zookeeper       (ut/->available-port)
               :schema-registry (ut/->available-port)}]
    {:zookareg.schema-registry/schema-registry {:ports      ports
                                                :_kafka     (ig/ref :zookareg.kafka/kafka)
                                                :_zookeeper (ig/ref :zookareg.zookeeper/zookeeper)}
     :zookareg.kafka/kafka                     {:ports      ports
                                                :_zookeeper (ig/ref :zookareg.zookeeper/zookeeper)}
     :zookareg.zookeeper/zookeeper             {:ports ports}}))

(defn halt-zookareg! []
  (when @state/system
    (swap! state/system ig/halt!)))

(defn init-zookareg []
  (let [config (->zookareg-config)]
    (clojure.pprint/pprint config)
    (try
      (halt-zookareg!)
      (ig/load-namespaces config)
      (reset! state/system (ig/init config))
      (catch clojure.lang.ExceptionInfo ex
        (ig/halt! (:system (ex-data ex)))
        (throw (.getCause ex))))))

(comment
  ;;;
  (init-zookareg)

  ;;;
)
