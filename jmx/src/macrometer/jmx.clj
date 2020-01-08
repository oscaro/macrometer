(ns macrometer.jmx
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [macrometer
             [core :as m :refer [default-registry]]
             [misc :refer [->duration]]
             [binders :as b]])
  (:import (io.micrometer.core.instrument Clock)
           (io.micrometer.jmx JmxConfig JmxMeterRegistry)))

(def config
  {:component/metrics {:domain  "metrics"
                       :global? true
                       :binders {:hotspot?   false
                                 :logging?   false
                                 :kafka?     false
                                 :executors? false}}})

(defn ^JmxMeterRegistry jmx-registry
  [domain]
  (let [cfg (proxy [JmxConfig] []
              (domain [] domain)
              (step [] (->duration [1 :minutes]))
              (get [_]))]
    (JmxMeterRegistry. cfg Clock/SYSTEM)))

(defn- add-binders
  [reg {:keys [hotspot? logging? kafka? executors?]}]
  (when hotspot? (b/add-hotspot-metrics reg))
  (when logging? (b/add-logging-metrics reg))
  (when kafka? (b/add-kafka-metrics reg))
  (when executors? (b/add-executor-metrics reg)))

(defmethod ig/init-key :component/metrics [_ {:keys [domain global? binders] :as sys}]
  (log/info "Starting jmx metrics component")
  (let [reg (jmx-registry domain)]
    (when global?
      (.add default-registry reg))
    (add-binders reg binders)
    (.start reg)
    (assoc sys :registry reg)))

(defmethod ig/halt-key! :component/metrics [_ {:keys [global? ^JmxMeterRegistry registry] :as sys}]
  (log/info "Stopping jmx metrics component")
  (when global?
    (m/clear-meters)
    (.remove default-registry registry))
  (.close registry)
  (dissoc sys :registry))
