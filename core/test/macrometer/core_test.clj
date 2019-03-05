(ns macrometer.core-test
  (:require [clojure.test :refer :all]
            [macrometer.core :refer :all])
  (:import (io.micrometer.core.instrument Tag)))

(deftest ->tags-test
  (are [tags expected]
    (= expected (->tags tags))
    nil []
    {} []
    {:a 1 :b 2} [(Tag/of "a" "1") (Tag/of "b" "2")]
    ; Null values are not allowed but should throw an NPE
    {"15365489631" nil} []))
