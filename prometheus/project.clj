(defproject com.oscaro/macrometer.prometheus "1.3.2.0"
  :dependencies [[com.oscaro/macrometer.core "1.3.2.0"]
                 [io.micrometer/micrometer-registry-prometheus "1.3.2"]
                 [io.prometheus/simpleclient_common "0.7.0"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev      {:dependencies [[io.pedestal/pedestal.service-tools "0.5.8"]
                                       [io.pedestal/pedestal.jetty "0.5.8"]]
                        :source-paths ["dev"]}
             :provided {:dependencies [[org.clojure/clojure "1.10.1"]
                                       [org.clojure/tools.logging "1.1.0"]
                                       [integrant "0.8.0"]]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
