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

#_(deftest add-hotspot-metrics-test
  (add-hotspot-metrics *registry*)
  (is (= #{"process.uptime" "jvm.max.memory" "jvm.gc.max.data.size" "jvm.threads.peak"
           "jvm.memory.used" "jvm.threads.live" "total.physical.memory" "jvm.gc.collection"
           "jvm.buffer.memory.used" "system.load.average.1m" "free.swap.space" "process.resident.memory"
           "jvm.threads.daemon" "process.files.max" "jvm.classes.loaded" "jvm.classes.unloaded"
           "jvm.memory.committed" "jvm.gc.live.data.size" "jvm.gc.memory.allocated"
           "committed.virtual.memory" "jvm.info" "jvm.buffer.total.capacity" "total.swap.space"
           "free.physical.memory" "jvm.gc.memory.promoted" "process.files.open" "jvm.memory.max"
           "process.virtual.memory" "system.cpu.count" "jvm.free.memory" "jvm.threads.states"
           "process.cpu.time" "process.cpu.usage" "jvm.total.memory" "system.cpu.usage" "process.start.time"
           "jvm.buffer.count"}
         (metric-names))))

#_(deftest monitor-executor-test
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
