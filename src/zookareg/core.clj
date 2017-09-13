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

(defmacro with-zookareg
  "Starts up zookareg with the specified configuration; executes the body then shuts down."
  [config & body]
  `(try
     (init-zookareg ~config)
     ~@body
     (finally
       (halt-zookareg!))))

(defn with-zookareg-test-fixture
  "Starts up zookareg with the specified configuration; executes the function then shuts down."
  ([config f]
   (with-zookareg config (f)))
  ([f]
   (with-zookareg default-config (f))))

(comment
  ;;;
  (init-zookareg {:ports {:kafka           9092
                          :zookeeper       2181
                          :schema-registry 8081}})
  (init-zookareg (->available-ports))
  (halt-zookareg!)
  (with-zookareg default-config
    (println "hi"))

  (with-zookareg-around #(println "hi"))

  (def ports (-> @zookareg.state/config
                 ut/disqualify-keys
                 :kafka
                 :ports))

  ports
  ;;;
  )
