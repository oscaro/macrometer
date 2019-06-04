(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [macrometer.core :as m]))

(comment
  (refresh)
  (m/all-meters)
  (m/clear-meters)
  )
