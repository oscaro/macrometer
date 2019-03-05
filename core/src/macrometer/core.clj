(ns macrometer.core
  (:import (io.micrometer.core.instrument MeterRegistry Metrics Tag)))

(def ^{:tag MeterRegistry :doc "Default registry used by public API functions when no explicit registry argument is given"}
default-registry Metrics/globalRegistry)

(defn- kv->tag [k v] (when v (Tag/of (name k) (str v))))
(defn ^Iterable ->tags
  "Convenience function for generating a sequence of tags"
  [tags]
  (let [add-tag (fn [tags [k v]]
                  (if-let [tag (kv->tag k v)]
                    (conj tags tag)
                    tags))]
    (reduce add-tag [] tags)))
