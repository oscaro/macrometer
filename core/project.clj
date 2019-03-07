(defproject com.oscaro/macrometer.core "1.1.3.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [io.micrometer/micrometer-core "1.1.3"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
