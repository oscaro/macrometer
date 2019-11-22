(ns macrometer.binders
  (:require [macrometer.core :refer [default-registry ->tags]])
  (:import (io.micrometer.core.instrument MeterRegistry)
           (io.micrometer.core.instrument.binder.jvm ClassLoaderMetrics JvmGcMetrics JvmMemoryMetrics JvmThreadMetrics ExecutorServiceMetrics)
           (io.micrometer.core.instrument.binder.system FileDescriptorMetrics ProcessorMetrics UptimeMetrics)
           (io.micrometer.core.instrument.binder MeterBinder)
           (io.micrometer.core.instrument.binder.kafka KafkaConsumerMetrics)
           (com.oscaro.micrometer.binders GCMetrics OSMetrics RuntimeMetrics)
           (java.util.concurrent Executor)))

(def logback-present?
  (try
    (import 'ch.qos.logback.classic.Logger)
    true
    (catch Throwable _ false)))

(defn add-hotspot-metrics
  "Adds hotspot metrics to the registry"
  ([] (add-hotspot-metrics default-registry))
  ([^MeterRegistry reg]
   (doseq [metrics [(ClassLoaderMetrics.)
                    (JvmGcMetrics.)
                    (JvmMemoryMetrics.)
                    (JvmThreadMetrics.)
                    (FileDescriptorMetrics.)
                    (GCMetrics.)
                    (OSMetrics.)
                    (ProcessorMetrics.)
                    (RuntimeMetrics.)
                    (UptimeMetrics.)]]
     (.bindTo ^MeterBinder metrics reg))))

(defn add-kafka-metrics
  "Adds kafka consumer metrics to the registry"
  ([] (add-kafka-metrics default-registry))
  ([^MeterRegistry reg]
   (.bindTo (KafkaConsumerMetrics.) reg)))

(defmacro add-logging-metrics
  "Adds logging (logback) metrics to the registry"
  ([] (add-logging-metrics default-registry))
  ([^MeterRegistry reg]
   (when logback-present?
     `(.bindTo (io.micrometer.core.instrument.binder.logging.LogbackMetrics.) ~reg))))

(defn monitor-executor
  "Record metrics on the use of an executor"
  ([n executor] (monitor-executor n executor nil))
  ([^String n ^Executor executor {:keys [registry tags] :or {registry default-registry}}]
   (ExecutorServiceMetrics/monitor ^MeterRegistry registry executor n (->tags tags))))
