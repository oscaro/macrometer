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
  (let [sys (ig/init config)]
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

(g/defgauge app-some-gauge
  :tags {:c "c" :d "d"}
  :description "This is a gauge"
  :unit "km/h")

(t/deftimer app-some-timer
  :tags {:e "e" :f "f"})

(defn filter-metric
  [metric body]
  (->> body
       split-lines
       (filter (fn [^String s] (.startsWith s metric)))))

(defn approximately=
  [precision m1 m2]
  (->> (merge-with (fn [& args] args) m1 m2)
       (map (fn [[k [v1 v2]]]
              (if (float? v1)
                (< (* precision v1) (Double/parseDouble v2) (* (+ 2 (- precision)) v1))
                (= v1 v2))))
       (doall)
       (every? true?)))

(deftest prometheus-metrics-test

  (testing "setting some values"
    (c/increment app-some-counter 100)
    (is (= 100.0 (c/count app-some-counter)))
    (swap! app-some-gauge inc)
    (is (= 1.0 @app-some-gauge))
    (let [f (t/wrapped app-some-timer (fn [] (Thread/sleep 100)))]
      (f))
    (is (< 10 (.totalTime app-some-timer java.util.concurrent.TimeUnit/MILLISECONDS) 1000)))

  (testing "/metrics route"
    (let [{:keys [status body]} (response-for *service* :get "/metrics")]
      (is (= 200 status))
      (is (= ["app_some_counter_total{a=\"a\",b=\"b\",} 100.0"]
             (filter-metric "app_some_counter" body)))
      (is (= ["app_some_gauge_km_h{c=\"c\",d=\"d\",} 1.0"]
             (filter-metric "app_some_gauge" body)))
      (is (approximately=
           0.8
           {"app_some_timer_seconds_max{e=\"e\",f=\"f\",}" 0.1
            "app_some_timer_seconds_count{e=\"e\",f=\"f\",}" "1.0"
            "app_some_timer_seconds_sum{e=\"e\",f=\"f\",}" 0.1}
           (->> (filter-metric "app_some_timer" body)
                (map #(vec (str/split % #" ")))
                (into {})))))))
