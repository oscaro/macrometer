(defproject com.oscaro/macrometer.prometheus "1.12.4.1-SNAPSHOT"
  :dependencies [[com.oscaro/macrometer.core                   "1.12.4.1-SNAPSHOT"]
                 [io.micrometer/micrometer-registry-prometheus "1.12.4"]]
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles {:dev      {:dependencies [[io.dropwizard.metrics/metrics-jmx  "4.2.25"]
                                       [io.pedestal/pedestal.service-tools "0.6.3"]
                                       [io.pedestal/pedestal.jetty         "0.6.3"]]
                        :source-paths ["dev"]}
             :provided {:dependencies [[org.clojure/clojure       "1.10.1"]
                                       [org.clojure/tools.logging "1.3.0"]
                                       [integrant                 "0.8.1"]]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
