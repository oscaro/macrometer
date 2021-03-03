(defproject com.oscaro/macrometer "1.6.3.1-SNAPSHOT"
  :plugins [[lein-sub "0.3.0"]]
  :sub ["core"
        "jmx"
        "prometheus"]
  :description "Clojure wrapper for http://micrometer.io/"
  :url "https://gitlab.oscaroad.com/it-dev/macrometer"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"})
