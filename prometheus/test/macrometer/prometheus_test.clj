(ns macrometer.prometheus-test
  (:require [clojure.test :refer :all]
            [macrometer.prometheus :refer :all]
            [clojure.string :refer [split-lines]]
            [integrant.core :as ig]
            [io.pedestal
             [http :as http]
             [test :refer [response-for]]]
            [macrometer
             [counters :as c]
             [gauges :as g]
             [timers :as t]]
            [clojure.string :as str]))

(def ^:dynamic *service* nil)

(defn with-service
  [f]
  (let [sys (ig/init (assoc-in config [:component/metrics :binders] {:hotspot? true}))]
    (try
      (binding [*service* (->> (:component/metrics sys)
                               (merge {::http/port 8888 ::http/type :jetty})
                               http/create-servlet
                               ::http/service-fn)]
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

(defn filter-metric
  [metric body]
  (->> body
       split-lines
       (filter (fn [^String s] (.startsWith s metric)))))

(defn- is-close-to [^double d1 ^double d2 err] (<= (Math/abs (- d1 d2)) err))
(defn approximately=
  [m1 m2 err]
  (->> (merge-with (fn [& args] args) m1 m2)
       (map (fn [[_ [v1 v2]]]
              (is-close-to (read-string v1) (read-string v2) err)))
       doall
       (every? true?)))

(deftest prometheus-metrics-test

  (testing "setting some values"
    (c/increment app-some-counter 100)
    (is (= 100.0 (c/count app-some-counter)))
    (swap! a inc)
    (is (= 1.0 (g/value app-some-gauge)))
    (let [f (t/monitor app-some-timer (fn [] (Thread/sleep 100)))]
      (f))
    (is (< 10 (t/total-time app-some-timer :milliseconds) 1000)))

  (testing "/metrics route"
    (let [{:keys [status body]} (response-for *service* :get "/metrics")]
      (is (= 200 status))
      (is (= ["app_some_counter_total{a=\"a\",b=\"b\",} 100.0"]
             (filter-metric "app_some_counter" body)))
      (is (= ["app_some_gauge_km_h{c=\"c\",d=\"d\",} 1.0"]
             (filter-metric "app_some_gauge" body)))
      (let [res (->> (filter-metric "app_some_timer" body)
                     (map #(vec (str/split % #" ")))
                     (into {}))]
        (is (approximately=
              {"app_some_timer_seconds_max{e=\"e\",f=\"f\",}"                       "0.1"
               "app_some_timer_seconds_count{e=\"e\",f=\"f\",}"                     "1.0"
               "app_some_timer_seconds_sum{e=\"e\",f=\"f\",}"                       "0.1"
               "app_some_timer_seconds{e=\"e\",f=\"f\",quantile=\"0.9\",}"          "0.096"
               "app_some_timer_seconds{e=\"e\",f=\"f\",quantile=\"0.95\",}"         "0.096"
               "app_some_timer_seconds_bucket{e=\"e\",f=\"f\",le=\"0.1\",}"         "0.0"
               "app_some_timer_seconds_bucket{e=\"e\",f=\"f\",le=\"0.111848106\",}" "1.0"
               "app_some_timer_seconds_bucket{e=\"e\",f=\"f\",le=\"0.12\",}"        "1.0"
               "app_some_timer_seconds_bucket{e=\"e\",f=\"f\",le=\"0.2\",}"         "1.0"
               "app_some_timer_seconds_bucket{e=\"e\",f=\"f\",le=\"+Inf\",}"        "1.0"}
              res
              0.01))))))
