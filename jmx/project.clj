(defproject com.oscaro/macrometer.jmx "1.1.4.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1-RC1" :scope "provided"]
                 [org.clojure/tools.logging "0.4.1"]
                 [com.oscaro/macrometer.core "1.1.4.0"]
                 [io.micrometer/micrometer-registry-jmx "1.1.4"]
                 [integrant "0.7.0"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/core.async "0.4.490"]
                                  [ch.qos.logback/logback-classic "1.2.3"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
