(defproject com.oscaro/macrometer.jmx "1.2.0.0"
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/tools.logging "0.5.0"]
                 [com.oscaro/macrometer.core "1.2.0.0"]
                 [io.micrometer/micrometer-registry-jmx "1.2.0"]
                 [io.dropwizard.metrics/metrics-jmx "4.1.0"]
                 [integrant "0.7.0"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/core.async "0.4.500"]
                                  [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})