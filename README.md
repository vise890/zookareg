# zookareg

Embedded `Zo`okeeper `Ka`fka and Confluent's Schema `Reg`istry.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/vise890/zookareg.svg)](https://clojars.org/vise890/zookareg)

```clojure
;; in project.clj

[vise890/zookareg "0.2.2"]
```

```clojure
;;; Development:
(require 'zookareg.core)

;; Start an embedded System with default ports:
;; zk: 2181 k: 9999 sreg: 8081
(init-zookareg)

;; another call will halt the previous system:
(init-zookareg)

;; Specify ports:
(init-zookareg {:ports {:kafka           9092
                        :zookeeper       2181
                        :schema-registry 8081}})

;; Random-ish available ports:
(init-zookareg (->available-ports))

;; When you're done:
(halt-zookareg!)

;;; Testing:
;;; NOTE: these will halt running zookareg instances

(clojure.test/use-fixtures :once with-zookareg-test-fixture)

;; if you want to have access to the config / running integrant system:
(with-zookareg
  (fn [config system]
    ,,,))
```

Happy testing!

## License

Copyright © 2017 Martino Visintin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
