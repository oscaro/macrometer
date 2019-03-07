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
             [gauges :as g]]))

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

(deftest prometheus-metrics-test

  (testing "setting some values"
    (c/increment app-some-counter 100)
    (is (= 100.0 (c/count app-some-counter)))
    (swap! app-some-gauge inc)
    (is (= 1.0 @app-some-gauge)))

  (testing "/metrics route"
    (let [{:keys [status body]} (response-for *service* :get "/metrics")]
      (is (= 200 status))
      (is (= #{"# HELP app_some_counter_total This is a counter"
               "# TYPE app_some_counter_total counter"
               "app_some_counter_total{a=\"a\",b=\"b\",} 100.0"
               "# HELP app_some_gauge_km_h This is a gauge"
               "# TYPE app_some_gauge_km_h gauge"
               "app_some_gauge_km_h{c=\"c\",d=\"d\",} 1.0"}
             (-> body split-lines set))))))
