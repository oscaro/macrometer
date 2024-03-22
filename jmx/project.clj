(defproject com.oscaro/macrometer.jmx "1.12.4.0"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.oscaro/macrometer.core            "1.12.4.0"]
                 [io.micrometer/micrometer-registry-jmx "1.12.4"]
                 [io.dropwizard.metrics/metrics-jmx     "4.2.25"]]
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles {:dev      {:dependencies [[org.clojure/tools.namespace    "1.5.0"]
                                       [ch.qos.logback/logback-classic "1.5.3" :exclusions [org.slf4j/slf4j-api]]]
                        :source-paths ["dev"]}
             :provided {:dependencies [[org.clojure/clojure       "1.10.1"]
                                       [org.clojure/tools.logging "1.3.0"]
                                       [integrant                 "0.8.1"]]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
