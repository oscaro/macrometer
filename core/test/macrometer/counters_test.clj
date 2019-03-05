(ns macrometer.counters-test
  (:refer-clojure :exclude [count])
  (:require [clojure.test :refer :all]
            [macrometer.counters :refer :all]
            [macrometer.test-helper :refer :all]))

(defcounter test-counters-defcounter
  :tags {:a "a" :b "b"})

(deftest counters-are-unique
  (is (identical?
        (counter "test.counters.defcounter" :tags {:a "a" :b "b"})
        test-counters-defcounter)))

(use-fixtures
  :each
  with-registry)

(deftest counters-test
  (let [c (counter "counter" :registry *registry*)]
    (is (zero? (count c)))
    (increment c)
    (is (= 1.0 (count c)))
    (increment c 10.5)
    (is (= 11.5 (count c)))))
