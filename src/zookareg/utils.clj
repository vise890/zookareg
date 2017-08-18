(ns zookareg.utils
  (:import java.io.FileOutputStream
           java.net.ServerSocket
           java.util.Properties))

(defn m->properties [m]
  (let [ps (Properties.)]
    (doseq [[k v] m] (.setProperty ps k v))
    ps))

(defn ->available-port []
  (with-open [s (ServerSocket. 0)]
    (.setReuseAddress s true)
    (.getLocalPort s)))

(defn store-properties
  [properties file]
  (with-open [out (FileOutputStream. file)]
    (.store properties
            out
            "Genereated by zookareg"))
  (.getAbsolutePath file))
