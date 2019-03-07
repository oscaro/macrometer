(defproject com.oscaro/macrometer.prometheus "1.1.3.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/tools.logging "0.4.1"]
                 [com.oscaro/macrometer.core "1.1.3.0-SNAPSHOT"]
                 [io.micrometer/micrometer-registry-prometheus "1.1.3"]
                 [integrant "0.7.0"]]
  :profiles {:dev {:dependencies [[io.pedestal/pedestal.service-tools "0.5.5"]
                                  [io.pedestal/pedestal.jetty "0.5.5"]
                                  [io.prometheus/simpleclient_hotspot "0.5.0"]]
                   :source-paths ["dev"]}}
  :global-vars {*warn-on-reflection* true})
