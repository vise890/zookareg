(ns zookareg.kafka
  (:require [integrant.core :as ig]
            [me.raynes.fs :as fs]
            [zookareg.utils :as ut])
  (:import [kafka.server KafkaConfig KafkaServerStartable]))

(defn ->kafka-config [{:keys [zookeeper
                              kafka]
                       :as   ports}
                      kafka-config]
  (merge {"broker.id"                   "0"
          "listeners"                   (str "PLAINTEXT://localhost:" kafka)
          "bootstrap.servers"           (str "localhost:" kafka)
          "zookeeper.connect"           (str "127.0.0.1:" zookeeper)
          "zookeeper-port"              (str zookeeper)
          "log.flush.interval.messages" "1"
          "auto.create.topics.enable"   "true"
          "group.id"                    "consumer"
          "auto.offset.reset"           "earliest"
          "retry.backoff.ms"            "500"
          "message.send.max.retries"    "5"
          "auto.commit.enable"          "false"
          "max.poll.records"            "1"
          "log.dir"                     (.getAbsolutePath (fs/temp-dir "kafka-log"))}
         kafka-config))

(defn ->broker [ports kafka-config]
  (let [config (KafkaConfig. (ut/m->properties (->kafka-config ports kafka-config)))
        server (KafkaServerStartable. config)]
    (.startup server)
    server))

(defn shutdown [b]
  (when b
    (.shutdown b)
    (.awaitShutdown b)))

(defmethod ig/init-key ::kafka [_ {:keys [ports
                                          kafka-config]}]
  (->broker ports kafka-config))

(defmethod ig/halt-key! ::kafka [_ b]
  (shutdown b))
