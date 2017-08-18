(ns zookareg.schema-registry
  (:require [integrant.core :as ig]
            [me.raynes.fs :as fs]
            [zookareg.utils :as ut])
  (:import [io.confluent.kafka.schemaregistry.rest SchemaRegistryConfig SchemaRegistryRestApplication]))

(defn ->config [{:keys [zookeeper
                        kafka
                        schema-registry]
                 :as   _ports}]
  {"listeners"                    (str "http://0.0.0.0:" schema-registry)
   "kafkastore.connection.url"    (str "localhost:" zookeeper)
   "kafkastore.bootstrap.servers" (str "PLAINTEXT://localhost:" kafka)
   "kafkastore.topic"             "_schemas"
   "debug"                        "false"})

(defn ->config-file [ports]
  (let [props (ut/m->properties (->config ports))
        file  (fs/temp-file "schema-registry" ".properties")]
    (ut/store-properties props file)
    file))

(defn ->schema-registry [ports]
  (let [config (SchemaRegistryConfig. (.getAbsolutePath (->config-file ports)))
        app    (SchemaRegistryRestApplication. config)
        server (.createServer app)]
    (.start server)
    server))

(defn halt! [server]
  (when server
    (.stop server)))

(defmethod ig/init-key ::schema-registry [_ {:keys [ports]}]
  (->schema-registry ports))

(defmethod ig/halt-key! ::schema-registry [_ server]
  (halt! server))

