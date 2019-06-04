(defproject com.oscaro/macrometer.core "1.1.4.0"
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [io.micrometer/micrometer-core "1.1.4"]
                 [com.oscaro/micrometer-binders "0.1.0"]]
  :repositories [["oscaro" {:url "https://artifactory.oscaroad.com/artifactory/libs-release-local"}]
                 ["oscaro-ext" {:url "https://artifactory.oscaroad.com/artifactory/ext-release-local"}]
                 ["oscaro-snapshot" {:url "https://artifactory.oscaroad.com/artifactory/libs-snapshot-local"}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/core.async "0.4.490"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
