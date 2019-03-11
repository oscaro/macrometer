(ns macrometer.core
  (:import (io.micrometer.core.instrument MeterRegistry Metrics Tag Meter)
           (io.micrometer.core.instrument.composite CompositeMeterRegistry)))

(def ^{:tag CompositeMeterRegistry :doc "Default registry used by public API functions when no explicit registry argument is given"}
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

(defn ^Meter register-meter
  "Convenience function for registering meters"
  [builder opts]
  (let [{:keys [tags description unit registry]
         :or   {registry default-registry}} opts]
    (cond-> builder
      tags (.tags (->tags tags))
      description (.description description)
      unit (.baseUnit unit)
      :always (.register registry))))

(defn unregister-meter
  "Convenience function for unregistering meters"
  ([m]
   (unregister-meter default-registry m))
  ([^MeterRegistry reg ^Meter m]
   (.remove reg m)))
