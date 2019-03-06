(ns macrometer.gauges-test
  (:require [clojure.test :refer :all]
            [macrometer.gauges :refer :all]
            [macrometer.test-helper :refer :all])
  (:import (java.util.concurrent.atomic AtomicLong)))

(defgauge test-gauges-defgauge
  rand
  :tags {:a "a" :b "b"})

(deftest counters-are-unique
  (is (identical?
        (gauge "test.gauges.defgauge" rand :tags {:a "a" :b "b"})
        test-gauges-defgauge)))

(use-fixtures
  :each
  with-registry)

(deftest gauges-test

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
      (is (zero? (.get a)))
      (.set a 10)
      (is (= 10.0 (value g)))))

  (testing "manual gauges are just atoms which are incremented
            and decremented manually (and report metrics of course ;))"
    (let [g (gauge "gauge.manual" :registry *registry*)]
      (is (zero? @g))
      (swap! g + 10)
      (is (= 10.0 @g))
      (reset! g 0)
      (is (zero? (value g))))))
