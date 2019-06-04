(ns macrometer.counters-test
  (:refer-clojure :exclude [count])
  (:require [clojure.test :refer :all]
            [macrometer.counters :refer :all]
            [macrometer.test-utils :refer :all]
            [macrometer.core :as m])
  (:import (io.micrometer.core.instrument.simple SimpleMeterRegistry)))

(def reg (SimpleMeterRegistry.))
(defcounter a-counter
  :tags {:a "a" :b "b"}
  :registry reg)

(use-fixtures
  :each
  with-registry)

(deftest counter-test
  (let [c (counter "cnt" :tags {:a "a"} :registry *registry*)]

    (testing "a new counter is always 0"
      (is (zero? (count c))))

    (testing "increment by 1"
      (increment c)
      (is (= 1.0 (count c))))

    (testing "increment by an arbitrary number"
      (increment c 10.5)
      (is (= 11.5 (count c))))))

(deftest defcounter-test

  (testing "instances are only created once"
    (is (= (mk-counter a-counter) (first (m/counters "a.counter" reg)))))

  (testing "simple operations"
    (is (zero? (count a-counter)))
    (increment a-counter)
    (is (= 1.0 (count a-counter)))
    (increment a-counter 10.5)
    (is (= 11.5 (count a-counter))))

  (testing "changing existing tags in fact creates a new meter underneath"
    (increment a-counter :a "c")
    (increment a-counter 2 :a "c")
    (is (= 3.0 (count a-counter :a "c")))
    (is (= 11.5 (count a-counter)) "Old meter without additional tags remains unchanged")))
