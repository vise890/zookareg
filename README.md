# zookareg

Embedded **Zo**okeeper **Ka**fka and Confluent's Schema **Reg**istry.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/vise890/zookareg.svg)](https://clojars.org/vise890/zookareg)
```clojure
;; in project.clj
[vise890/zookareg "0.4.5"]
```

### Development:

```clojure
(require 'zookareg.core)

;; Start an embedded system with default ports:
;; zookeeper: 2181; kafka: 9092; schema registry: 8081
(init-zookareg)

;; another call will halt the previous system:
(init-zookareg)

;; When you're done:
(halt-zookareg!)
```

### Testing:

**NOTE**: these will halt running zookareg instances

```clojure
(require 'clojure.test)

(use-fixtures :once with-zookareg-fn)

(defn around-all
  [f]
  (with-zookareg-fn (merge default-config
                           {:ports {:kafka 8888}})
                    f))

(use-fixtures :once around-all)

;;; You can also wrap ad-hoc code in zookareg init/halt:
(with-zookareg default-config
	,,, :do-something ,,,)
```

### Other Goodies

```clojure
;; Specify ports:
(init-zookareg {:ports {:kafka           9092
                        :zookeeper       2181
                        :schema-registry 8081}})

;; Random-ish available ports:
(init-zookareg (->available-ports))
```

Happy testing!

## License

Copyright © 2017 Martino Visintin & Contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
