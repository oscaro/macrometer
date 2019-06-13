(ns macrometer.jmx-test
  (:require [clojure.test :refer :all]
            [macrometer.jmx :refer :all]
            [integrant.core :as ig]
            [macrometer
             [counters :as c]
             [gauges :as g]
             [timers :as t]]))

(def ^:dynamic *service* nil)

(defn with-service
  [f]
  (let [sys (ig/init (assoc-in config [:component/metrics :binders] {:hotspot? true}))]
    (try
      (binding [*service* (->> (:component/metrics sys))]
        (f))
      (finally
        (ig/halt! sys)))))

(use-fixtures
  :each
  with-service)

(c/defcounter app-some-counter
  :tags {:a "a" :b "b"}
  :description "This is a counter")

(def a (atom 0))
(g/defgauge app-some-gauge
  a
  :tags {:c "c" :d "d"}
  :description "This is a gauge"
  :unit "km/h")

(t/deftimer app-some-timer
  :tags {:e "e" :f "f"}
  :description "This is a timer"
  :publish-percentiles [0.9 0.95]
  :percentile-precision (int 1)
  :publish-percentile-histogram true
  :sla [[100 :millis] [200 :millis]]
  :minimum-expected-value [100 :millis]
  :maximum-expected-value [120 :millis])

(deftest prometheus-metrics-test

  (testing "setting some values"
    (c/increment app-some-counter 100)
    (is (= 100.0 (c/count app-some-counter)))
    (swap! a inc)
    (is (= 1.0 (g/value app-some-gauge)))
    (let [f (t/monitor app-some-timer (fn [] (Thread/sleep 100)))]
      (f))
    (is (< 10 (t/total-time app-some-timer :milliseconds) 1000))))
