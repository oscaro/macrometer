(defproject com.oscaro/macrometer.core "1.12.4.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure           "1.10.1" :scope "provided"]
                 [org.clojure/core.async        "1.6.681"]
                 [io.micrometer/micrometer-core "1.12.4"]]
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace    "1.5.0"]
                                  [org.eclipse.jetty/jetty-server "12.0.7"]
                                  [clj-http                       "3.12.3"]]
                   :source-paths ["dev"]}}
  :repl-options {:init-ns user}
  :global-vars {*warn-on-reflection* true})
