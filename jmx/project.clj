(defproject com.oscaro/macrometer.jmx "1.3.2.0-SNAPSHOT"
  :dependencies [[com.oscaro/macrometer.core "1.3.2.0-SNAPSHOT"]
                 [io.micrometer/micrometer-registry-jmx "1.3.2"]
                 [io.dropwizard.metrics/metrics-jmx "4.1.13"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev      {:dependencies [[org.clojure/tools.namespace "1.0.0"]
                                       [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]]
                        :source-paths ["dev"]}
             :provided {:dependencies [[org.clojure/clojure "1.10.1"]
                                       [org.clojure/tools.logging "1.1.0"]
                                       [integrant "0.8.0"]]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
