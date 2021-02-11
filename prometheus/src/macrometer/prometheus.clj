(ns macrometer.prometheus
  (:require [clojure.datafy :refer [datafy]]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [macrometer
             [core :as m :refer [default-registry]]
             [binders :as b]])
  (:import (io.micrometer.core.instrument Clock)
           (io.micrometer.prometheus PrometheusMeterRegistry PrometheusConfig)
           (io.prometheus.client CollectorRegistry)))

(def config
  {:component/metrics {:route   "/metrics"
                       :global? true
                       :binders {:hotspot?   false
                                 :logging?   false
                                 :kafka?     false
                                 :executors? false}}})

(defn- scrape [^PrometheusMeterRegistry reg] (.scrape reg))

(defn- prometheus-metrics
  [reg]
  {:name  :prometheus-metrics
   :enter (fn [ctx] (assoc ctx :response {:status 200 :body (scrape reg)}))})

(defn- reitit-metrics
  [reg _]
  {:status 200 :body (scrape reg)})

(defn- prometheus-registry
  []
  (PrometheusMeterRegistry.
    PrometheusConfig/DEFAULT
    CollectorRegistry/defaultRegistry
    Clock/SYSTEM))

(defn- add-binders
  [reg {:keys [hotspot? logging? kafka? executors?]}]
  (when hotspot? (b/add-hotspot-metrics reg))
  (when logging? (b/add-logging-metrics reg))
  (when kafka? (b/add-kafka-metrics reg))
  (when executors? (b/add-executor-metrics reg)))

(defmethod ig/init-key :component/metrics [_ {:keys [route global? binders] :as sys}]
  (log/info "Starting prometheus metrics component")
  (let [reg (prometheus-registry)]
    (when global?
      (.add default-registry reg))
    (add-binders reg binders)
    (assoc sys
      :registry reg
      :expose (fn expose-metrics [] (scrape reg))
      :io.pedestal.http/routes #{[route :get (prometheus-metrics reg)]}
      :reitit.http/routes [[route {:get (partial reitit-metrics reg)}]])))

(defmethod ig/halt-key! :component/metrics [_ {:keys [global? ^PrometheusMeterRegistry registry] :as sys}]
  (log/info "Stopping prometheus metrics component")
  (when global?
    (m/clear-meters)
    (.remove default-registry registry))
  (.close registry)
  (.clear (.getPrometheusRegistry registry))
  (dissoc sys :registry :io.pedestal.http/routes))
