(defproject vise890/zookareg "0.1.1"
  :description "Embedded `Zo`okeeper `Ka`fka and Confluent's Schema `Reg`istry"
  :url "http://github.com/vise890/zookareg"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories {"confluent" "http://packages.confluent.io/maven"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [integrant "0.6.0"]

                 [org.clojure/tools.logging "0.4.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [me.raynes/fs "1.4.6"]

                 [io.confluent/kafka-schema-registry "3.3.0"
                  :exclusions [org.apache.kafka/kafka-clients
                               org.apache.kafka/kafka_2.11]]
                 [org.apache.kafka/kafka_2.11 "0.10.2.1"
                  :exclusions [org.apache.zookeeper/zookeeper]]
                 [org.apache.curator/curator-test "4.0.0"]])
