(defproject com.oscaro/macrometer.prometheus "1.3.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/tools.logging "0.5.0"]
                 [com.oscaro/macrometer.core "1.3.1.0-SNAPSHOT"]
                 [io.micrometer/micrometer-registry-prometheus "1.3.1"]
                 [integrant "0.7.0"]
                 [io.prometheus/simpleclient_common "0.7.0"]
                 [io.pedestal/pedestal.service "0.5.7" :exclusions [org.clojure/core.async] :scope "provided"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/core.async "0.5.527"]
                                  [io.pedestal/pedestal.service-tools "0.5.7"]
                                  [io.pedestal/pedestal.jetty "0.5.7"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
