(ns macrometer.core
  (:require [clojure.core.protocols :refer [Datafiable]]
            [clojure.datafy :refer [datafy]]
            [macrometer.misc :as m])
  (:import (io.micrometer.core.instrument Counter
                                          Gauge
                                          Measurement
                                          Meter
                                          Meter$Id
                                          MeterRegistry
                                          Metrics
                                          Tag
                                          Timer)
           (io.micrometer.core.instrument.composite CompositeMeterRegistry)))

(def
  ^{:tag CompositeMeterRegistry
    :doc "Default registry used by public API functions when no explicit registry argument is given"}
  default-registry Metrics/globalRegistry)

(defn- kv->tag [k v] (when (some? v) (Tag/of (name k) (str v))))
(defn- tag->kv [^Tag t] [(keyword (.getKey t)) (.getValue t)])
(defn ->tags
  "Convenience function for generating a sequence of tags"
  ^Iterable [tags]
  (let [add-tag (fn [tags k v]
                  (if-let [tag (kv->tag k v)]
                    (conj tags tag)
                    tags))]
    (reduce-kv add-tag [] tags)))

(defn unregister-meter
  "Removes a meter"
  ([m] (unregister-meter default-registry m))
  ([^MeterRegistry reg ^Meter m]
   (.remove reg m)))

(defn all-meters
  "Returns all registered meters"
  ([] (all-meters default-registry))
  ([^MeterRegistry reg]
   (seq (.getMeters reg))))

(defn clear-meters
  "Removes all meters from the registry"
  ([] (clear-meters default-registry))
  ([^MeterRegistry reg]
   (doseq [^Meter m (all-meters reg)]
     (unregister-meter reg m))))

(defn- meters-by-name
  [c n ^MeterRegistry reg]
  (->> (all-meters reg)
       (filter (partial instance? c))
       (filter (fn [^Meter m] (= n (.getName (.getId m)))))))

(defn counters
  "Returns all counters given a name (and registry)"
  ([n] (counters n default-registry))
  ([n ^MeterRegistry reg]
   (meters-by-name Counter n reg)))

(defn gauges
  "Returns all gauges given a name (and registry)"
  ([n] (gauges n default-registry))
  ([n ^MeterRegistry reg]
   (meters-by-name Gauge n reg)))

(defn timers
  "Returns all timers given a name (and registry)"
  ([n] (timers n default-registry))
  ([n ^MeterRegistry reg]
   (meters-by-name Timer n reg)))

(extend-protocol Datafiable

  Measurement
  (datafy [x]
    {:stat (-> x .getStatistic .getTagValueRepresentation keyword)
     :val  (.getValue x)})

  Meter
  (datafy [x]
    {:id      (datafy (.getId x))
     :measure (map datafy (.measure x))})

  Meter$Id
  (datafy [x]
    (let [desc (.getDescription x)
          unit (.getBaseUnit x)]
      (cond-> {:name (.getName x)
               :type (m/enum->kw (.getType x))
               :tags (into {} (map tag->kv) (.getTagsAsIterable x))}
        desc (assoc :description desc)
        unit (assoc :unit unit)))))
