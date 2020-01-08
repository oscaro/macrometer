(ns macrometer.misc
  (:require [clojure.core.protocols :refer [Datafiable]]
            [clojure.string :as str])
  (:import (java.time Duration)
           (java.time.temporal ChronoUnit)
           (java.util EnumSet)
           (java.util.concurrent TimeUnit)))

(defn enum->kw
  [^Enum e]
  (-> (.name e)
      str/lower-case
      (str/replace \_ \-)
      keyword))

(defn enums-as-map
  [c]
  (reduce
    (fn [m e] (assoc m (enum->kw e) e))
    {}
    (EnumSet/allOf c)))

(def time-units (enums-as-map TimeUnit))
(def chrono-units (enums-as-map ChronoUnit))
(defn ->duration
  "Convenience method for converting a human friendly duration into a Duration.
  Allowed units are :hours, :minutes, :seconds, :millis and :nanos.

  ex. (->duration [10 :seconds])
   => #object[java.time.Duration 0x4d156026 \"PT10S\"]"
  [[amt unit]]
  (Duration/of amt (get chrono-units unit)))

(extend-protocol Datafiable
  Duration
  (datafy [x] {:seconds (.getSeconds x) :nanos (.getNano x)}))
