(ns macrometer.jmx
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [macrometer
             [core :as m :refer [default-registry]]
             [binders :as b]])
  (:import (io.micrometer.core.instrument Clock)
           (io.micrometer.jmx JmxConfig JmxMeterRegistry)
           (java.time Duration)))

(def config
  {:component/metrics {:domain  "metrics"
                       :global? true
                       :binders {:hotspot? false
                                 :logging? false
                                 :kafka?   false}}})

(defn ^JmxMeterRegistry jmx-registry
  [domain]
  (let [cfg (proxy [JmxConfig] []
              (domain [] domain)
              (step [] (Duration/ofMinutes 1))
              (get [_]))]
    (JmxMeterRegistry. cfg Clock/SYSTEM)))

(defn- add-binders
  [reg {:keys [hotspot? logging? kafka?]}]
  (when hotspot? (b/add-hotspot-metrics reg))
  (when logging? (b/add-logging-metrics reg))
  (when kafka? (b/add-kafka-metrics reg)))

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
