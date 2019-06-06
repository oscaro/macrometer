(defproject com.oscaro/macrometer.prometheus "1.1.4.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1-RC1" :scope "provided"]
                 [org.clojure/tools.logging "0.4.1"]
                 [com.oscaro/macrometer.core "1.1.4.0"]
                 [io.micrometer/micrometer-registry-prometheus "1.1.4"]
                 [integrant "0.7.0"]
                 [io.prometheus/simpleclient_common "0.6.0"]
                 [io.pedestal/pedestal.service "0.5.5" :scope "provided"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[io.pedestal/pedestal.service-tools "0.5.5"]
                                  [io.pedestal/pedestal.jetty "0.5.5"]
                                  [io.prometheus/simpleclient_hotspot "0.6.0"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
