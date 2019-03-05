(ns macrometer.test-helper
  (:require [clojure.test :refer :all])
  (:import (io.micrometer.core.instrument.simple SimpleMeterRegistry)
           (io.micrometer.core.instrument MeterRegistry)))

(def ^:dynamic ^MeterRegistry *registry* nil)

(defn with-registry
  [f]
  (binding [*registry* (SimpleMeterRegistry.)]
    (f)
    (.close *registry*)))
