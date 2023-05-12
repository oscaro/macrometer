(ns macrometer.binders
  (:require [macrometer.core :refer [default-registry ->tags]])
  (:import io.micrometer.core.instrument.MeterRegistry
           io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
           io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
           io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
           io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
           io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics
           io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
           io.micrometer.core.instrument.binder.system.ProcessorMetrics
           io.micrometer.core.instrument.binder.system.UptimeMetrics
           io.micrometer.core.instrument.binder.MeterBinder
           io.micrometer.core.instrument.binder.kafka.KafkaConsumerMetrics
           java.util.concurrent.Executor
           clojure.lang.Agent))

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
                    (ProcessorMetrics.)
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

(defn add-executor-metrics
  "Adds executor metrics (where possible) to the registry"
  ([] (add-executor-metrics default-registry))
  ([^MeterRegistry reg]
   (let [set-executor! (fn [f n e] (f (monitor-executor n e {:registry reg})))]
     (set-executor! set-agent-send-executor! "agent-send-executor" Agent/pooledExecutor)
     (set-executor! set-agent-send-off-executor! "agent-send-off-executor" Agent/soloExecutor))))

(defmacro ^:private add-jetty-monitor-fn []
  (when (try
          (import '(org.eclipse.jetty.server Server)
                  '(io.micrometer.core.instrument.binder.jetty JettyServerThreadPoolMetrics JettyConnectionMetrics))
          true
          (catch ClassNotFoundException _ false))
    '(defn monitor-jetty
       "Adds metrics for a jetty server
  Requires jetty server in the classpath"
       ([server] (monitor-jetty server {}))
       ([server {:keys [tags registry] :or {registry default-registry tags {}}}]
        (let [^Server server server]
          (doto (JettyServerThreadPoolMetrics. (.getThreadPool server) (->tags tags))
            (.bindTo registry))
          (JettyConnectionMetrics/addToAllConnectors server registry (->tags tags)))))))

(add-jetty-monitor-fn)

(defmacro ^:private add-monitor-conn-manager-fn []
  (when (try
          (import '(org.apache.http.pool ConnPoolControl)
                  '(io.micrometer.core.instrument.binder.httpcomponents PoolingHttpClientConnectionManagerMetricsBinder))
          true
          (catch ClassNotFoundException _ false))
    '(defn monitor-conn-manager
       "Adds metrics for a syncronous or asynchronous http connection manager
  Requires apache http in the classpath"
       ([conn-mgr meter-name] (monitor-conn-manager conn-mgr meter-name {}))
       ([conn-mgr ^String meter-name {:keys [tags registry] :or {registry default-registry tags {}}}]
        (doto (PoolingHttpClientConnectionManagerMetricsBinder. ^ConnPoolControl conn-mgr meter-name (->tags tags))
          (.bindTo registry))))))

(add-monitor-conn-manager-fn)
