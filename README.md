# zookareg

Embedded `Zo`okeeper `Ka`fka and Confluent's Schema `Reg`istry.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/vise890/zookareg.svg)](https://clojars.org/vise890/zookareg)

```clojure
;; in project.clj
[vise890/zookareg "0.1.0"]
```

```clojure
(require 'zookareg.core)

;; Start an embedded System with default ports:
;; zk: 2181 k: 9999 sreg: 8081
(init-zookareg)

;; another call will halt the previous system:
(init-zookareg)

;; Specify ports:
(init-zookareg {:kafka           1234
                :zookeeper       2222
                :schema-registry 8080})

;; Random-ish available ports:
(init-zookareg (->available-ports))

;; When you're done:
(halt-zookareg!)
```

Happy testing!

## License

Copyright © 2017 Martino Visintin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
