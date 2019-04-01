(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [macrometer.core :refer :all]))

(comment
  (refresh)
  (all-meters)
  (clear-meters)
  )
