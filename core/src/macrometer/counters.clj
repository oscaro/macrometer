(ns macrometer.counters
  (:refer-clojure :exclude [count])
  (:require [clojure.string :as s]
            [macrometer.core :refer [default-registry ->tags]])
  (:import (io.micrometer.core.instrument Counter)))

(defn counter
  "Add the counter (a monotonically increasing value given a name n a sequence of tags) to a single registry,
  or return an existing counter in that registry.
  The returned counter will be unique for each registry, but each registry is guaranteed to only create
  one counter for the same combination of name and tags.

  ex. (counter \"http.request.count\" :tags {:route \"/api/users\" :method \"GET\"})"
  [n & opts]
  (let [{:keys [tags description unit registry]
         :or   {registry default-registry}} (apply array-map opts)]
    (cond-> (Counter/builder n)
      tags (.tags (->tags tags))
      description (.description description)
      unit (.baseUnit unit)
      :always (.register registry))))

(defmacro defcounter
  "Defines a new counter metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)
  Invocations are this macro will always add the metric to the global (ie. default) registry)"
  [s & opts]
  (let [n (s/replace (str s) "-" ".")
        args (vec (cons n opts))]
    `(def ~s (apply counter ~args))))

(defn count
  "Returns the cumulative count since this counter was created."
  [^Counter c]
  (.count c))

(defn increment
  "Updates the counter by amount or 1.0 if not specified."
  ([^Counter c]
   (.increment c))
  ([^Counter c amt]
   (.increment c amt)))
