(defproject com.oscaro/macrometer.core "1.6.3.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/core.async "1.3.610"]
                 [io.micrometer/micrometer-core "1.6.3"]
                 [com.oscaro/micrometer-binders "0.1.1"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.0.0"]
                                  [org.eclipse.jetty/jetty-server "9.4.36.v20210114"]
                                  [clj-http "3.11.0"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
