(ns macrometer.timers-test
  (:refer-clojure :exclude [count])
  (:require [clojure.test :refer :all]
            [macrometer.timers :refer :all]
            [macrometer.test-utils :refer :all]
            [macrometer.core :as m])
  (:import (io.micrometer.core.instrument.simple SimpleMeterRegistry SimpleConfig)
           (io.micrometer.core.instrument MockClock)))

(def reg (SimpleMeterRegistry. SimpleConfig/DEFAULT (MockClock.)))
(deftimer a-timer
  :tags {:a "a" :b "b"}
  :registry reg)

(use-fixtures
  :each
  with-clock
  with-registry)

(deftest timer-test
  (let [t (timer "tmr" :tags {:a "a"} :registry *registry*)]

    (testing "a new timer always has a count and total time of zero"
      (is (zero? (count t)))
      (is (zero? (total-time t :milliseconds))))

    (testing "recording time can be set manually"
      (record t 3 :seconds)
      (is (= 1 (count t)))
      (is (= 3000.0 (total-time t :milliseconds))))

    (testing "functions can be recorded"
      (is (true? (record-fn t (fn [] (fast-forward 10 :seconds) true))))
      (is (= 2 (count t)))
      (is (= 13.0 (total-time t :seconds))))

    (testing "blocks of code as well"
      (is (false? (dorecord t
                    (fast-forward 47 :seconds)
                    false)))
      (is (= 3 (count t)))
      (is (= 1.0 (total-time t :minutes))))

    (testing "functions may be become permanently monitored (ie. wrapped)"
      (let [f (monitor t #(fast-forward 1 :minutes))]
        (dotimes [_ 3] (f))
        (is (= 6 (count t)))
        (is (= 4.0 (total-time t :minutes)))))

    (testing "async operations are best performed using sampling"
      (let [s (start *registry*)
            f (fn []
                (fast-forward 56 :minutes)
                (stop t s))]
        @(future-call f)
        (is (= 7 (count t)))
        (is (= 1.0 (total-time t :hours)))))))

(deftest deftimer-test

  (testing "instances are only created once"
    (is (= (mk-timer a-timer) (first (m/timers "a.timer" reg)))))

  (testing "simple operations"
    (is (zero? (count a-timer)))
    (is (zero? (total-time a-timer :milliseconds)))
    (record a-timer 1 :microseconds)
    (is (= 1 (count a-timer)))
    (is (= 1.0 (total-time a-timer :microseconds)))
    (record-fn a-timer #(fast-forward reg 999 :microseconds))
    (is (= 2 (count a-timer)))
    (is (= 1.0 (total-time a-timer :milliseconds)))
    (let [f (monitor a-timer #(fast-forward reg 999 :milliseconds))]
      (f)
      (is (= 3 (count a-timer)))
      (is (= 1.0 (total-time a-timer :seconds))))
    (let [s (start reg)
          f (fn []
              (fast-forward reg 59 :seconds)
              (stop a-timer s))]
      @(future-call f)
      (is (= 4 (count a-timer)))
      (is (= 1.0 (total-time a-timer :minutes)))))

  (testing "adding new tags in fact creates a new meter underneath"
    (record a-timer 1 :minutes :c "c")
    (record-fn a-timer #(fast-forward reg 1 :minutes) :c "c")
    (let [s (start reg)]
      (fast-forward reg 1 :minutes)
      (stop a-timer s :c "c"))
    (is (= 3 (count a-timer :c "c")))
    (is (= 3.0 (total-time a-timer :minutes :c "c")))
    (is (= 1.0 (total-time a-timer :minutes)) "Old meter without additional tags remains unchanged")))
