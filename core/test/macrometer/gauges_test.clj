(ns macrometer.gauges-test
  (:require [clojure.test :refer :all]
            [macrometer.gauges :refer :all]
            [macrometer.test-utils :refer :all]
            [clojure.core.async :as a]
            [macrometer.core :as m])
  (:import (java.util.concurrent.atomic AtomicLong)
           (io.micrometer.core.instrument.simple SimpleMeterRegistry)))

(def a (atom 0))
(def reg (SimpleMeterRegistry.))
(defgauge a-gauge
  a
  :tags {:a "a" :b "b"}
  :registry reg)

(use-fixtures
  :each
  with-registry)

(deftest gauge-test

  (testing "arbitrary functions"
    (let [x (rand-int 100)
          g (gauge "gauge.fn" (constantly x) :registry *registry*)]
      (is (= (double x) (value g)))))

  (testing "support for atoms"
    (let [a (atom 0)
          g (gauge "gauge.ref" a :registry *registry*)]
      (is (zero? (value g)))
      (swap! a inc)
      (is (= 1.0 (value g)))
      (swap! a + 10)
      (is (= 11.0 (value g)))
      (reset! a 0)
      (is (zero? (value g)))))

  (testing "support for atomic number values"
    (let [a (AtomicLong.)
          g (gauge "gauge.long" a :registry *registry*)]
      (is (zero? (value g)))
      (.set a 10)
      (is (= 10.0 (value g)))))

  (testing "if core.async is present, gauges on internal buffers is supported"
    (let [ch (a/chan 10)
          g  (gauge "gauge.chan" ch :registry *registry*)]
      (is (zero? (value g)))
      (dotimes [i 10]
        (a/>!! ch i))
      (a/close! ch)
      (is (= 10.0 (value g)))
      (loop [] (when-let [_ (a/<!! ch)] (recur)))
      (is (zero? (value g))))))

(deftest defgauge-test

  (testing "instances are only created once"
    (is (= a-gauge (first (m/gauges "a.gauge" reg)))))

  (testing "simple operations"
    (is (zero? (value a-gauge)))
    (swap! a inc)
    (is (= 1 @a))
    (is (= 1.0 (value a-gauge)) "Gauges are always doubles")
    (swap! a + 10)
    (is (= 11.0 (value a-gauge)))
    (reset! a 0)
    (is (zero? (value a-gauge)))))
