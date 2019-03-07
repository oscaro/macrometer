(ns macrometer.prometheus
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [macrometer.core :refer [default-registry]])
  (:import (io.micrometer.core.instrument Clock)
           (io.micrometer.prometheus PrometheusMeterRegistry PrometheusConfig)
           (io.prometheus.client CollectorRegistry)))

(def config
  {:component/metrics {:route            "/metrics"
                       :global?          true
                       :include-hotspot? false}})

(defn- prometheus-metrics
  [^PrometheusMeterRegistry reg]
  {:name  :prometheus-metrics
   :enter (fn [ctx] (assoc ctx :response {:status 200 :body (.scrape reg)}))})

(defn prometheus-registry
  []
  (PrometheusMeterRegistry.
    PrometheusConfig/DEFAULT
    CollectorRegistry/defaultRegistry
    Clock/SYSTEM))

(defn- add-hotspot-metrics
  []
  (log/info "Adding hotspot metrics")
  (try
    (Class/forName "io.prometheus.client.hotspot.DefaultExports")
    (eval `(io.prometheus.client.hotspot.DefaultExports/initialize))
    (catch Exception _
      (log/warn "io.prometheus/simpleclient_hotspot library is not loaded into classpath"))))

(defmethod ig/init-key :component/metrics [_ {:keys [route global? include-hotspot?] :as sys}]
  (log/info "Starting prometheus metrics component")
  (let [reg    (prometheus-registry)
        routes #{[route :get (prometheus-metrics reg)]}]
    (when global?
      (.add default-registry reg))
    (when include-hotspot?
      (add-hotspot-metrics))
    (assoc sys
      :registry reg
      :io.pedestal.http/routes routes)))

(defmethod ig/halt-key! :component/metrics [_ {:keys [global? ^PrometheusMeterRegistry registry] :as sys}]
  (log/info "Stopping prometheus metrics component")
  (when global?
    (.remove default-registry registry))
  (.close registry)
  (.clear (.getPrometheusRegistry registry))
  (dissoc sys :registry :io.pedestal.http/routes))
