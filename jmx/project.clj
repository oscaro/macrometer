(defproject com.oscaro/macrometer.jmx "1.6.3.1"
  :dependencies [[com.oscaro/macrometer.core            "1.6.3.1"]
                 [io.micrometer/micrometer-registry-jmx "1.6.3"]
                 [io.dropwizard.metrics/metrics-jmx     "4.1.13"]]
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles {:dev      {:dependencies [[org.clojure/tools.namespace    "1.0.0"]
                                       [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]]
                        :source-paths ["dev"]}
             :provided {:dependencies [[org.clojure/clojure       "1.10.1"]
                                       [org.clojure/tools.logging "1.1.0"]
                                       [integrant                 "0.8.0"]]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
