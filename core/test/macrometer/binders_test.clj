(ns macrometer.binders-test
  (:require [clojure.test :refer :all]
            [macrometer.binders :refer :all]
            [macrometer.test-utils :refer :all]
            [clojure.datafy :refer [datafy]]
            [macrometer.core :as m]
            [clj-http.conn-mgr :as conn-mgr])
  (:import (java.util.concurrent Executors ExecutorService)
           (org.eclipse.jetty.server Server)))

(use-fixtures
  :each
  with-registry)

(defn- metric-names
  []
  (->> (m/all-meters *registry*)
       (map datafy)
       (map :id)
       (map :name)
       set))

(deftest add-hotspot-metrics-test
  (add-hotspot-metrics *registry*)
  (is (= #{"committed.virtual.memory"
           "free.physical.memory"
           "free.swap.space"
           "jvm.buffer.count"
           "jvm.buffer.memory.used"
           "jvm.buffer.total.capacity"
           "jvm.classes.loaded"
           "jvm.classes.unloaded"
           "jvm.free.memory"
           "jvm.gc.collection"
           "jvm.gc.live.data.size"
           "jvm.gc.max.data.size"
           "jvm.gc.memory.allocated"
           "jvm.gc.memory.promoted"
           "jvm.info"
           "jvm.max.memory"
           "jvm.memory.committed"
           "jvm.memory.max"
           "jvm.memory.used"
           "jvm.threads.daemon"
           "jvm.threads.live"
           "jvm.threads.peak"
           "jvm.threads.states"
           "jvm.total.memory"
           "process.cpu.time"
           "process.cpu.usage"
           "process.files.max"
           "process.files.open"
           "process.resident.memory"
           "process.start.time"
           "process.uptime"
           "process.virtual.memory"
           "system.cpu.count"
           "system.cpu.usage"
           "system.load.average.1m"
           "total.physical.memory"
           "total.swap.space"}
         (metric-names))))

(deftest monitor-executor-test
  (let [^ExecutorService executor (Executors/newSingleThreadScheduledExecutor)]
    (monitor-executor "test" executor {:registry *registry*})
    (is (= #{"executor"
             "executor.active"
             "executor.completed"
             "executor.idle"
             "executor.pool.core"
             "executor.pool.max"
             "executor.pool.size"
             "executor.queue.remaining"
             "executor.queued"
             "executor.scheduled.once"
             "executor.scheduled.repetitively"}
           (metric-names)))))

(deftest monitor-jetty-test
  (let [server (Server. ^long (+ 50000 (rand-int 5000)))
        t (future (.start server))]
    (try
      (monitor-jetty server {:registry *registry*})
      (is (= #{"jetty.connections.messages.out"
               "jetty.threads.jobs"
               "jetty.threads.config.min"
               "jetty.connections.bytes.out"
               "jetty.connections.messages.in"
               "jetty.connections.bytes.in"
               "jetty.threads.config.max"
               "jetty.connections.max"
               "jetty.threads.current"
               "jetty.threads.idle"
               "jetty.threads.busy"
               "jetty.connections.current"}
             (metric-names)))
      (finally (future-cancel t)))))

(deftest monitor-conn-manager-test
  (let [conn-mgr (conn-mgr/make-reusable-conn-manager {})]
    (monitor-conn-manager conn-mgr "my-conn" {:registry *registry*})
    (is (= #{"httpcomponents.httpclient.pool.total.connections"
             "httpcomponents.httpclient.pool.total.max"
             "httpcomponents.httpclient.pool.total.pending"
             "httpcomponents.httpclient.pool.route.max.default"}
           (metric-names)))))
