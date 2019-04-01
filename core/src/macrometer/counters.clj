(ns macrometer.counters
  (:refer-clojure :exclude [count])
  (:require [clojure.string :as s]
            [macrometer.core :refer :all])
  (:import (io.micrometer.core.instrument Counter)
           (clojure.lang IPersistentMap)))

(defn ^Counter mk-counter
  "Defines a new counter"
  ([c] (mk-counter c nil))
  ([{:keys [name tags description unit registry]
     :or   {registry default-registry}} more-tags]
   (cond-> (Counter/builder name)
     tags (.tags (->tags tags))
     more-tags (.tags (->tags (apply array-map more-tags)))
     description (.description description)
     unit (.baseUnit unit)
     registry (.register registry))))

(defn ^Counter counter
  "Defines a new counter (a monotonically increasing value given a name n a sequence of tags) to a single registry,
  or return an existing counter in that registry.
  The returned counter will be unique for each registry, but each registry is guaranteed to only create
  one counter for the same combination of name and tags.

  ex. (counter \"http.request.count\" :tags {:route \"/api/users\" :method \"GET\"})"
  [^String n & opts]
  (mk-counter (assoc (apply array-map opts) :name n)))

(defmulti count
  "Returns the cumulative count since this counter was created."
  (fn [c & _] (class c)))
(defmethod count Counter
  [^Counter c]
  (.count c))
(defmethod count IPersistentMap
  [c & tags]
  (count (mk-counter c tags)))

(defmulti increment
  "Updates the counter by amount or 1.0 if not specified."
  (fn [c & _] (class c)))
(defmethod increment Counter
  ([^Counter c]
    (.increment c))
  ([^Counter c amt]
    (.increment c amt)))
(defmethod increment IPersistentMap
  [c & args]
  (if (odd? (clojure.core/count args))
    (let [amt  (double (first args))
          tags (rest args)]
      (increment (mk-counter c tags) amt))
    (increment (mk-counter c args))))

(defmacro defcounter
  "Defines a new counter metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)."
  [s & opts]
  (let [n (s/replace (name s) "-" ".")]
    `(def ~s ~(assoc (apply array-map opts) :name n))))
