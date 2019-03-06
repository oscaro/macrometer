(ns macrometer.counters
  (:refer-clojure :exclude [count])
  (:require [clojure.string :as s]
            [macrometer.core :refer [register-meter]])
  (:import (io.micrometer.core.instrument Counter FunctionCounter)
           (java.util.function ToDoubleFunction)))

(defn counter
  "Defines a new counter (a monotonically increasing value given a name n a sequence of tags) to a single registry,
  or return an existing counter in that registry.
  The returned counter will be unique for each registry, but each registry is guaranteed to only create
  one counter for the same combination of name and tags.

  ex. (counter \"http.request.count\" :tags {:route \"/api/users\" :method \"GET\"})"
  [^String n & opts]
  (register-meter (Counter/builder n) opts))

(defmacro defcounter
  "Defines a new counter metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)
  Invocations are this macro will always add the metric to the global (ie. default) registry)"
  [s & opts]
  (let [n (s/replace (name s) "-" ".")]
    `(def ~s (counter ~n ~@opts))))

(defmulti count
  "Returns the cumulative count since this counter was created."
  class)
(defmethod count Counter [^Counter c] (.count c))
(defmethod count FunctionCounter [^FunctionCounter c] (.count c))

(defn increment
  "Updates the counter by amount or 1.0 if not specified."
  ([^Counter c]
   (.increment c))
  ([^Counter c amt]
   (.increment c amt)))

(defn fn-counter
  "Defines a new function-tracking counter where obj is the state of a specific obj
  and f a monotonically increasing function.
  It is very important that f is guaranteed to be monotonic.
  see. http://micrometer.io/docs/concepts#_function_tracking_counters"
  [^String n obj f & opts]
  (let [dbl-fn (reify ToDoubleFunction (applyAsDouble [_ v] (double (f v))))]
    (register-meter (FunctionCounter/builder n obj dbl-fn) opts)))
