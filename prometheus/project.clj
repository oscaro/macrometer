(defproject com.oscaro/macrometer.prometheus "1.6.3.1"
  :dependencies [[com.oscaro/macrometer.core                   "1.6.3.1"]
                 [io.micrometer/micrometer-registry-prometheus "1.6.3"]]
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles {:dev      {:dependencies [[io.dropwizard.metrics/metrics-jmx  "4.1.13"]
                                       [io.pedestal/pedestal.service-tools "0.5.8"]
                                       [io.pedestal/pedestal.jetty         "0.5.8"]]
                        :source-paths ["dev"]}
             :provided {:dependencies [[org.clojure/clojure       "1.10.1"]
                                       [org.clojure/tools.logging "1.1.0"]
                                       [integrant                 "0.8.0"]]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
