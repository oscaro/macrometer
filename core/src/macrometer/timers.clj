(ns macrometer.timers
  (:refer-clojure :exclude [count])
  (:require [clojure.string :as s]
            [macrometer.core :refer :all]
            [macrometer.misc :as m])
  (:import (io.micrometer.core.instrument Timer MeterRegistry Timer$Sample)
           (java.time Duration)
           (clojure.lang IPersistentMap)))

(defn ^Timer mk-timer
  "Defines a new timer"
  ([t] (mk-timer t nil))
  ([{:keys [name tags description registry
            publish-percentiles percentile-precision publish-percentile-histogram
            sla minimum-expected-value maximum-expected-value
            distribution-statistic-expiry distribution-statistic-buffer-length pause-detector]
     :or   {registry default-registry}} more-tags]
   (cond-> (Timer/builder name)
     tags (.tags (->tags tags))
     more-tags (.tags (->tags (apply array-map more-tags)))
     description (.description description)
     publish-percentiles (.publishPercentiles (double-array publish-percentiles))
     percentile-precision (.percentilePrecision (int percentile-precision))
     publish-percentile-histogram (.publishPercentileHistogram publish-percentile-histogram)
     sla (.sla (->> sla (map m/->duration) (into-array Duration)))
     minimum-expected-value (.minimumExpectedValue (m/->duration minimum-expected-value))
     maximum-expected-value (.maximumExpectedValue (m/->duration maximum-expected-value))
     distribution-statistic-expiry (.distributionStatisticExpiry (m/->duration distribution-statistic-expiry))
     distribution-statistic-buffer-length (.distributionStatisticBufferLength (int distribution-statistic-buffer-length))
     pause-detector (.pauseDetector pause-detector)
     registry (.register registry))))

(defn ^Timer timer
  "Defines a new timer (a stateful construct useful for measuring short-duration latencies and the frequency
  of such events) to a single registry, or return an existing counter in that registry.
  The returned timer will be unique for each registry, but each registry is guaranteed to only create
  one timer for the same combination of name and tags.

  ex. (timer \"http.request.latency\" :tags {:route \"/api/users\" :method \"GET\"})"
  [^String n & opts]
  (mk-timer (assoc (apply array-map opts) :name n)))

(defmulti count
  "Returns he number of times that stop has been called on this timer"
  (fn [t & _] (class t)))
(defmethod count Timer
  [^Timer t]
  (.count t))
(defmethod count IPersistentMap
  [t & tags]
  (count (mk-timer t tags)))

(defmulti total-time
  "Returns the total time of recorded events.
  Allowed units are :nanoseconds, :microseconds, :milliseconds, :seconds, :minutes, :hours and :days."
  (fn [t & _] (class t)))
(defmethod total-time Timer
  [^Timer t unit]
  (.totalTime t (get m/time-units unit)))
(defmethod total-time IPersistentMap
  [t unit & tags]
  (total-time (mk-timer t tags) unit))

(defmulti record
  "Updates the statistics kept by the timer with the specified amount.
  Allowed units are :nanoseconds, :microseconds, :milliseconds, :seconds, :minutes, :hours and :days."
  (fn [t & _] (class t)))
(defmethod record Timer
  [^Timer t amt unit]
  (.record t amt (get m/time-units unit)))
(defmethod record IPersistentMap
  [t amt unit & tags]
  (record (mk-timer t tags) amt unit))

(defmulti record-fn
  "Executes the function and records the time taken"
  (fn [t & _] (class t)))
(defmethod record-fn Timer
  [^Timer t f]
  (.recordCallable t f))
(defmethod record-fn IPersistentMap
  [t f & tags]
  (record-fn (mk-timer t tags) f))

(defmulti monitor
  "Wraps a function so that it is timed when invoked"
  (fn [t & _] (class t)))
(defmethod monitor Timer
  [^Timer t ^Callable f]
  (let [c (.wrap t f)]
    (fn [] (.call c))))
(defmethod monitor IPersistentMap
  [t f & tags]
  (monitor (mk-timer t tags) f))

(defn ^Timer$Sample start
  "Start a timing sample"
  ([] (start default-registry))
  ([^MeterRegistry reg]
   (Timer/start reg)))

(defmulti stop
  "Records the duration of the operation"
  (fn [t & _] (class t)))
(defmethod stop Timer
  [^Timer t ^Timer$Sample smpl]
  (.stop smpl t))
(defmethod stop IPersistentMap
  [t smpl & tags]
  (stop (mk-timer t tags) smpl))

(defmacro dorecord
  "Executes the statement body and records the time taken."
  [^Timer t & body]
  `(record-fn ~t (fn [] ~@body)))

(defmacro deftimer
  "Defines a new timer metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)."
  [s & opts]
  (let [n (s/replace (name s) \- \.)]
    `(def ~s ~(assoc (apply array-map opts) :name n))))
