(defproject com.oscaro/macrometer "1.11.0.0-SNAPSHOT"
  :plugins [[lein-sub "0.3.0"]]
  :sub ["core"
        "jmx"
        "prometheus"]
  :description "Clojure wrapper for http://micrometer.io/"
  :url "https://github.com/oscaro/macrometer"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"})
