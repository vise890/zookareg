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

(defn- ->ports [zookeeper kafka schema-registry]
  {:ports {:kafka           kafka
           :zookeeper       zookeeper
           :schema-registry schema-registry}})

(defn ->available-ports-config []
  (merge default-config
         (->ports (ut/->available-port)
                  (ut/->available-port)
                  (ut/->available-port))))

(defn ->ig-config [config]
  {:zookareg.schema-registry/schema-registry
   {:ports      (:ports config)
    :_kafka     (ig/ref :zookareg.kafka/kafka)
    :_zookeeper (ig/ref :zookareg.zookeeper/zookeeper)}

   :zookareg.kafka/kafka
   {:ports        (:ports config)
    :kafka-config (:kafka-config config)
    :_zookeeper   (ig/ref :zookareg.zookeeper/zookeeper)}

   :zookareg.zookeeper/zookeeper
   {:ports (:ports config)}})

(defn halt-zookareg! []
  (when @state/state
    (swap! state/state
           (fn [s]
             (ig/halt! (:system s))
             nil))))

(defn init-zookareg
  ([] (init-zookareg default-config))
  ([config]
   (let [ig-config (->ig-config config)
         config-pp (with-out-str (pprint/pprint config))]
     (log/info "starting ZooKaReg with config:" config-pp)
     (try
       (halt-zookareg!)
       (ig/load-namespaces ig-config)
       ;; TODO stick in the same atom!
       (reset! state/state
               {:system (ig/init ig-config)
                :config ig-config})
       (catch clojure.lang.ExceptionInfo ex
         ;; NOTE tears down partially initialised system
         (ig/halt! (:system (ex-data ex)))
         (throw (.getCause ex)))))))

(defn with-zookareg-fn
  "Starts up zookareg with the specified configuration; executes the function then shuts down."
  ([config f]
   (try
     (init-zookareg config)
     (f)
     (finally
       (halt-zookareg!))))
  ([f]
   (with-zookareg-fn default-config f)))

(defmacro with-zookareg
  "Starts up zookareg with the specified configuration; executes the body then shuts down."
  [config & body]
  `(with-zookareg-fn ~config (fn [] ~@body)))
