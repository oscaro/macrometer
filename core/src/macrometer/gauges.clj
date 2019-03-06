(ns macrometer.gauges
  (:require [clojure.string :as s]
            [macrometer.core :refer [register-meter]])
  (:import (io.micrometer.core.instrument Gauge)
           (java.util.function Supplier ToDoubleFunction)
           (clojure.lang IFn IAtom IDeref)))

(defmulti gauge
  "Defines a new gauge (a value that may go up or down) to a single registry,
  or return an existing counter in that registry.
  The returned gauge will be unique for each registry, but each registry is guaranteed to only create
  one gauge for the same combination of name and tags.

  ex. (gauge \"pool.size\" (some-fn) :tags {:name \"work\"})"
  (fn [_ x & _]
    (cond
      (fn? x) :fn
      (instance? IAtom x) :atom
      (number? x) :number
      :else :default)))

(defmethod gauge :fn
  [^String n ^IFn f & opts]
  (let [supplier (reify Supplier (get [_] (double (f))))]
    (register-meter (Gauge/builder n supplier) opts)))

(def ^:private ^ToDoubleFunction atom->double
  (reify ToDoubleFunction (applyAsDouble [_ v] (-> v deref double))))
(defmethod gauge :atom
  [^String n ^IAtom a & opts]
  (register-meter (Gauge/builder n a atom->double) opts))

(def ^:private ^ToDoubleFunction num->double
  (reify ToDoubleFunction (applyAsDouble [_ v] (.doubleValue ^Number v))))
(defmethod gauge :number
  [^String n ^Number num & opts]
  (register-meter (Gauge/builder n num num->double) opts))

(deftype ManualGauge [^Gauge g ^IAtom a]
  IAtom
  (swap [_ f] (swap! a f))
  (swap [_ f x] (swap! a f x))
  (swap [_ f x y] (swap! a f x y))
  (swap [_ f x y args] (apply swap! a f x y args))
  (compareAndSet [_ old new] (compare-and-set! a old new))
  (reset [_ new] (reset! a new))
  IDeref
  (deref [_] (deref a))
  Gauge
  (value [_] (.value g)))

(defmethod gauge :default
  [^String n & opts]
  (let [a (atom 0.0)
        g (apply gauge n a opts)]
    (ManualGauge. g a)))

(defmacro defgauge
  "Defines a new gauge metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)
  Invocations are this macro will always add the metric to the global (ie. default) registry)"
  [s x & opts]
  (let [n (s/replace (name s) "-" ".")]
    `(def ~s (gauge ~n ~x ~@opts))))

(defn value
  "The act of observing the value by calling this method triggers sampling of the underlying number
  or user-defined function that defines the value for the gauge."
  [^Gauge g]
  (.value g))
