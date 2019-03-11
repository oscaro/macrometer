(ns macrometer.timers-test
  (:refer-clojure :exclude [count])
  (:require [clojure.test :refer :all]
            [macrometer.timers :refer :all]
            [macrometer.test-helper :refer :all])
  (:import (java.util.concurrent TimeUnit)
           (java.time Duration)))

(deftimer test-timers-deftimer
  :tags {:a "a" :b "b"}
  :publish-percentile-histogram true
  :publish-percentiles [0.5 0.95]
  :sla [(Duration/ofMillis 100)]
  :minimum-expected-value (Duration/ofMillis 1)
  :maximum-expected-value (Duration/ofMillis 1000))

(deftest timers-are-unique
  (is (identical?
       (timer "test.timers.deftimer"
              :tags {:a "a" :b "b"}
              :publish-percentile-histogram true
              :publish-percentiles [0.5 0.95]
              :sla [(Duration/ofMillis 100)]
              :minimum-expected-value (Duration/ofMillis 1)
              :maximum-expected-value (Duration/ofMillis 1000))
       test-timers-deftimer)))

(use-fixtures
  :each
  with-registry)

(def ms TimeUnit/MILLISECONDS)

(deftest timers-test
  (let [t (timer "counter" :registry *registry*)]
    (is (zero? (count t)))
    (is (zero? (.totalTime t ms)))
    (.record t 3000 ms)
    (is (= 1 (count t)))
    (is (== 3000 (.totalTime t ms))))
  (let [t (timer "counter2" :registry *registry*)
        f (monitored t (fn [] (Thread/sleep 100) true))]
    (is (zero? (count t)))
    (is (zero? (.totalTime t ms)))
    (is (f))
    (is (= 1 (count t)))
    (is (< 10 (.totalTime t ms) 1000)))
  (let [t (timer "counter3" :registry *registry*)]
    (is (zero? (count t)))
    (is (zero? (.totalTime t ms)))
    (is (monitor t (Thread/sleep 100) true))
    (is (= 1 (count t)))
    (is (< 10 (.totalTime t ms) 1000))))
    
