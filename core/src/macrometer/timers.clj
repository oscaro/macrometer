(ns macrometer.timers
  (:refer-clojure :exclude [count])
  (:require [clojure.string :as s]
            [macrometer.core :refer [register-meter]])
  (:import (io.micrometer.core.instrument Timer Timer$Sample)
           (io.micrometer.core.instrument MeterRegistry)
           (java.time Duration)))

(defn ^Timer timer
  "Defines a new timer"
  [^String n & opts]
  (let [{:keys [publish-percentile-histogram
                publish-percentiles
                sla
                maximum-expected-value
                minimum-expected-value]} opts]
    (cond-> (Timer/builder n)
      publish-percentile-histogram (.publishPercentileHistogram)
      publish-percentiles (.publishPercentiles (double-array publish-percentiles))
      sla (.sla (into-array Duration sla))
      minimum-expected-value (.minimumExpectedValue minimum-expected-value)
      maximum-expected-value (.maximumExpectedValue maximum-expected-value)
      :always (register-meter opts))))

(defmacro deftimer
  "Defines a new timer metric using the symbol as the name.
  For simplicity, all dashes are translated into dots (idiomatic)
  Invocations are this macro will always add the metric to the global (ie. default) registry)"
  [s & opts]
  (let [n (s/replace (name s) "-" ".")]
    `(def ~s (timer ~n ~@opts))))

(defn wrapped
  "Returns a monitored 0-arity function"
  [^Timer tmr ^Callable f]
  (let [callable (.wrap tmr f)]
    (fn []
      (.call callable))))

(defn monitor*
  [^Timer tmr ^Callable f]
  (.recordCallable tmr f))

(defn monitored
  "Returns a monitored function"
  [^Timer tmr ^Callable f]
  (fn [& args]
    (monitor* tmr ^Callable (fn [] (apply f args)))))

(defmacro monitor
  "Monitor the body in a timer."
  [^Timer tmr & body]
  `(monitor* ~tmr (fn [] ~@body)))

(defn ^Timer$Sample start
  "Register an anonymous timer sample in the registry."
  [^MeterRegistry reg]
  (Timer/start reg))

(defn stop
  "Save the sample result to a timer.
  The timer is set here and not at the start to allow using a result dependant timer"
  [^Timer$Sample sample ^Timer tmr]
  (.stop sample tmr))

(defn count
  [^Timer t]
  (.count t))
