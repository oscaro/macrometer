(ns macrometer.test-utils
  (:require [macrometer.misc :as m])
  (:import (io.micrometer.core.instrument MeterRegistry Clock MockClock)
           (io.micrometer.core.instrument.simple SimpleMeterRegistry SimpleConfig)))

(def ^:dynamic ^Clock *clock* Clock/SYSTEM)
(def ^:dynamic ^MeterRegistry *registry* nil)

(defn with-clock
  "Useful for mocking time spent"
  [f]
  (binding [*clock* (MockClock.)]
    (f)))

(defn with-registry
  "Helper fixture for setting up a in memory registry for testing"
  [f]
  (binding [*registry* (SimpleMeterRegistry. SimpleConfig/DEFAULT *clock*)]
    (f)
    (.close *registry*)))

(defn- add [^MockClock clock amt unit]
  (.add ^MockClock clock amt (get m/time-units unit))
  nil)
(defn fast-forward
  ([amt unit] (add *clock* amt unit))
  ([^MeterRegistry reg amt unit] (add (.clock (.config reg)) amt unit)))
