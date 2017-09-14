(ns zookareg.core-test
  (:require [clojure.test :refer :all]
            [zookareg.state :as zookareg.state]
            [zookareg.core :as sut]))

(defn state-matches? [expected-config]
  (is (= (sut/->ig-config expected-config)
         (:config @zookareg.state/state)))
  (is (:system @zookareg.state/state)))

(defn state-lifecycle-works?-fn
  "test that the stored ig-config is derived from the
  provided zookareg config and that state and config are reset
  after run"
  [config= f]
  (f)
  (state-matches? config=)
  (sut/halt-zookareg!)
  (is (nil? @zookareg.state/state)))

(defmacro state-lifecycle-works? [config= & body]
  `(state-lifecycle-works?-fn ~config= (fn [] ~@body)))

(deftest can-init
  (testing "uses default config by default"
    (state-lifecycle-works?
     sut/default-config
     (sut/init-zookareg)))

  (testing "with random-ish free ports"
    (let [config= (sut/->available-ports-config)]
      (state-lifecycle-works?
       config=
       (sut/init-zookareg config=))))

  (testing "with custom ports"
    (let [config= {:ports {:kafka           9999
                           :zookeeper       2222
                           :schema-registry 8888}}]
      (state-lifecycle-works?
       config=
       (sut/init-zookareg config=)))))

(deftest can-wrap-around
  (testing "using defaults"
    (sut/with-zookareg-fn
      #(state-matches? sut/default-config)))

  (testing "with specified config"
    (let [config= (sut/->available-ports-config)]
      (sut/with-zookareg-fn
        config=
        #(state-matches? config=)))))

(deftest can-wrap-around-macro
  (sut/with-zookareg sut/default-config
    (state-matches? sut/default-config))

  (testing "with specified config"
    (let [config= (sut/->available-ports-config)]
      (sut/with-zookareg config=
         (state-matches? config=)))))

