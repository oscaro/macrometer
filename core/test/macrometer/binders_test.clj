(ns macrometer.binders-test
  (:require [clojure.test :refer :all]
            [macrometer.binders :refer :all]
            [macrometer.test-utils :refer :all]
            [macrometer.core :as m])
  (:import (io.micrometer.core.instrument Meter Meter$Id)
           (java.util.concurrent Executors ExecutorService)))

(use-fixtures
  :each
  with-registry)

(defn- metric-names
  []
  (->> (m/all-meters *registry*)
       (map #(.getId ^Meter %))
       (map #(.getName ^Meter$Id %))
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
             "executor.pool.size"
             "executor.queue.remaining"
             "executor.queued"
             "executor.scheduled.once"
             "executor.scheduled.repetitively"}
           (metric-names)))))
