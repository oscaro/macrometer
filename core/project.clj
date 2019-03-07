(defproject com.oscaro/macrometer.core "1.1.3.0"
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [io.micrometer/micrometer-core "1.1.3"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
