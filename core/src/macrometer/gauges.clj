(ns macrometer.gauges
  (:require [clojure.string :as s]
            [macrometer.core :refer :all])
  (:import (io.micrometer.core.instrument Gauge Gauge$Builder)
           (java.util.function Supplier ToDoubleFunction)
           (clojure.lang IAtom Fn)))

(def core-async-present?
  (try
    (require 'clojure.core.async)
    true
    (catch Throwable _ false)))

(defn- mk-supplier [f] (reify Supplier (get [_] (f))))
(defn- mk-dbl-fn [f] (reify ToDoubleFunction (applyAsDouble [_ v] (f v))))
(defn ^Gauge mk-gauge
  "Defines a new gauge"
  ([n f opts] (mk-gauge (Gauge/builder n (mk-supplier f)) opts))
  ([n obj f opts] (mk-gauge (Gauge/builder ^String n obj ^ToDoubleFunction (mk-dbl-fn f)) opts))
  ([^Gauge$Builder builder {:keys [tags description unit strong registry]
                            :or   {registry default-registry}}]
   (cond-> builder
     tags (.tags (->tags tags))
     description (.description description)
     unit (.baseUnit unit)
     strong (.strongReference strong)
     registry (.register registry))))

(defmulti gauge
  "Defines a new gauge (a value that may go up or down) to a single registry,
  or return an existing counter in that registry.
  The returned gauge will be unique for each registry, but each registry is guaranteed to only create
  one gauge for the same combination of name and tags.

  ex. (gauge \"pool.size\" (some-fn) :tags {:name \"work\"})"
  (fn [_ x & _] (class x)))
(defmethod gauge Fn
  [^String n ^Fn f & opts]
  (mk-gauge n f opts))
(defmethod gauge IAtom
  [^String n ^IAtom a & opts]
  (mk-gauge n a deref opts))
(defmethod gauge Number
  [^String n ^Number number & opts]
  (mk-gauge n number #(.doubleValue ^Number %) opts))
(when core-async-present?
  (defmethod gauge clojure.core.async.impl.channels.ManyToManyChannel
    [^String n ^clojure.core.async.impl.channels.ManyToManyChannel ch & opts]
    (mk-gauge n (.buf ch) count opts)))

(defmacro defgauge
  "Defines a new gauge metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)"
  [s x & opts]
  (let [n (s/replace (name s) "-" ".")]
    `(def ~s (gauge ~n ~x ~@opts))))

(defn value
  "The act of observing the value by calling this method triggers sampling of the underlying number
  or user-defined function that defines the value for the gauge."
  [^Gauge g]
  (.value g))
