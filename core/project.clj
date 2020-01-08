(defproject com.oscaro/macrometer.core "1.3.2.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [io.micrometer/micrometer-core "1.3.2"]
                 [com.oscaro/micrometer-binders "0.1.1"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.3.1"]
                                  [org.clojure/core.async "0.5.527"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
