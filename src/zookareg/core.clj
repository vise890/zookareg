(ns zookareg.core
  (:require [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [zookareg.state :as state]
            [zookareg.utils :as ut]))

(def default-config
  {:ports        {:kafka           9092
                  :zookeeper       2181
                  :schema-registry 8081}
   :kafka-config {}})

(defn ->ports [zookeeper kafka schema-registry]
  {:ports {:kafka           kafka
           :zookeeper       zookeeper
           :schema-registry schema-registry}})

(defn ->available-ports []
  (->ports (ut/->available-port)
           (ut/->available-port)
           (ut/->available-port)))

(defn ->zookareg-config [default-config]
  {:zookareg.schema-registry/schema-registry {:ports      (:ports default-config)
                                              :_kafka     (ig/ref :zookareg.kafka/kafka)
                                              :_zookeeper (ig/ref :zookareg.zookeeper/zookeeper)}
   :zookareg.kafka/kafka                     {:ports        (:ports default-config)
                                              :kafka-config (:kafka-config default-config)
                                              :_zookeeper   (ig/ref :zookareg.zookeeper/zookeeper)}
   :zookareg.zookeeper/zookeeper             {:ports (:ports default-config)}})

(defn halt-zookareg! []
  (when @state/system
    (swap! state/system ig/halt!)))

(defn init-zookareg
  ([] (init-zookareg default-config))
  ([default-config] (let [config    (->zookareg-config default-config)
                          config-pp (with-out-str (pprint/pprint config))]
                      (log/info "starting ZooKaReg with config:" config-pp)
                      (try
                        (halt-zookareg!)
                        (ig/load-namespaces config)
                        (reset! state/system (ig/init config))
                        (reset! state/config config)
                        (catch clojure.lang.ExceptionInfo ex
                          ;; NOTE tear down partially initialised system
                          (ig/halt! (:system (ex-data ex)))
                          (throw (.getCause ex)))))))

(defn with-zookareg
  "Executes f within the context of an embedded zookareg. f will be passed 2 args: config and system"
  [f]
  (try
    (init-zookareg)
    (f @state/config @state/system)
    (finally
      (halt-zookareg!))))

(defn with-zookareg-test-fixture
  "Executes f within the context of an embedded zookareg. To be used as a `clojure.test` fixture"
  [f]
  (try
    (init-zookareg)
    (f)
    (finally
      (halt-zookareg!))))

(comment
  ;;;
  (init-zookareg {:ports {:kafka           9092
                          :zookeeper       2181
                          :schema-registry 8081}})
  (init-zookareg (->available-ports))
  (halt-zookareg!)
  (with-zookareg
    (fn [_ _] (println "hi")))

  (def ports (-> @zookareg.state/config
                 ut/disqualify-keys
                 :kafka
                 :ports))

  ports
  ;;;
  )
