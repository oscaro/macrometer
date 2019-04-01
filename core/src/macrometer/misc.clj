(ns macrometer.misc
  (:require [clojure.string :as str])
  (:import (io.micrometer.core.instrument Meter Meter$Id Measurement Tag)
           (java.time Duration)
           (java.time.temporal ChronoUnit)
           (java.util EnumSet)
           (java.util.concurrent TimeUnit)))

(defn- enum->kw
  [^Enum e]
  (-> (.name e)
      str/lower-case
      (str/replace \_ \-)
      keyword))

(defn- enums-as-map
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

; Helper methods for debugging
(defmethod print-method Meter [^Meter m w]
  (print-method {:id (.getId m) :measure (seq (.measure m))} w))

(defn- add-tag [m ^Tag t] (assoc m (keyword (.getKey t)) (.getValue t)))
(defmethod print-method Meter$Id [^Meter$Id id w]
  (let [desc (.getDescription id)
        unit (.getBaseUnit id)]
    (print-method
      (cond-> {:name (.getName id)
               :type (enum->kw (.getType id))
               :tags (reduce add-tag {} (.getTagsAsIterable id))}
        desc (assoc :description desc)
        unit (assoc :unit unit))
      w)))

(defmethod print-method Measurement [^Measurement m w]
  (print-method
    {:stat (enum->kw (.getStatistic m))
     :val  (.getValue m)}
    w))
