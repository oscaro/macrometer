(ns macrometer.prometheus
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [macrometer
             [core :as m :refer [default-registry]]
             [binders :as b]])
  (:import (io.micrometer.core.instrument Clock)
           (io.micrometer.prometheus PrometheusMeterRegistry PrometheusConfig)
           (io.prometheus.client CollectorRegistry)))

(def config
  {:component/metrics {:route   "/metrics"
                       :global? true
                       :binders {:hotspot? false
                                 :logging? false
                                 :kafka?   false}}})

(defn- prometheus-metrics
  [^PrometheusMeterRegistry reg]
  {:name  :prometheus-metrics
   :enter (fn [ctx] (assoc ctx :response {:status 200 :body (.scrape reg)}))})

(defn- json-metrics
  [reg]
  {:name  :json-metrics
   :enter (fn [ctx] (assoc ctx :response {:status 200
                                          :body   (-> (m/all-meters reg)
                                                      pr-str
                                                      clojure.edn/read-string)}))})

(defn- prometheus-registry
  []
  (PrometheusMeterRegistry.
    PrometheusConfig/DEFAULT
    CollectorRegistry/defaultRegistry
    Clock/SYSTEM))

(defn- add-binders
  [reg {:keys [hotspot? logging? kafka?]}]
  (when hotspot? (b/add-hotspot-metrics reg))
  (when logging? (b/add-logging-metrics reg))
  (when kafka? (b/add-kafka-metrics reg)))

(defmethod ig/init-key :component/metrics [_ {:keys [route global? binders] :as sys}]
  (log/info "Starting prometheus metrics component")
  (let [reg (prometheus-registry)]
    (when global?
      (.add default-registry reg))
    (add-binders reg binders)
    (assoc sys
      :registry reg
      :io.pedestal.http/routes #{[route :get (prometheus-metrics reg)]
                                 [(str route ".json") :get [http/json-body (json-metrics reg)]]})))

(defmethod ig/halt-key! :component/metrics [_ {:keys [global? ^PrometheusMeterRegistry registry] :as sys}]
  (log/info "Stopping prometheus metrics component")
  (when global?
    (m/clear-meters)
    (.remove default-registry registry))
  (.close registry)
  (.clear (.getPrometheusRegistry registry))
  (dissoc sys :registry :io.pedestal.http/routes))
