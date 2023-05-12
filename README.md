# macrometer [![Clojure Umbrella](https://github.com/oscaro/macrometer/actions/workflows/clojure.yml/badge.svg?branch=master)](https://github.com/oscaro/macrometer/actions/workflows/clojure.yml)

<a href="https://github.com/oscaro/macrometer"><img
  src="https://raw.githubusercontent.com/oscaro/macrometer/master/.github/logo.png"
  height="180" align="right"></a>

Clojure wrapper for http://micrometer.io/

## Usage

### [Counters](http://micrometer.io/docs/concepts#_counters)

Counters report a single metric, a count.
The Counter interface allows you to increment by a fixed amount, which must be positive.

```clojure
(require '[macrometer.counters :as c])

(c/defcounter a-counter
  :tags {:a "a" :b "b"}
  :description "A counter for something")

;(def a-counter (c/counter "a.counter" 
;                          :tags {:a "a" :b "b"}
;                          :description "A counter for something")) 
  
(c/increment a-counter)
(c/count a-counter)
; => 1.0
```

### [Gauges](http://micrometer.io/docs/concepts#_gauges)

A gauge is a handle to get the current value.
Typical examples for gauges would be the size of a collection or map or number of threads in a running state.

Never gauge something you can count with a Counter!

##### Reference tracking gauges
For `atom`s, `AtomicLong`s and other immutable `Number`s etc...

```clojure
(require '[macrometer.gauges :as g])

(let [a (atom 0)
      g (g/gauge "ext.gauge" a)]
  (swap! a + 10)
  (g/value g))
; => 10.0
```

##### Function gauges 

```clojure
; A random walking gauge ;)
(g/gauge "fn.gauge" (partial rand-int 100) :tags {:app "clio"})
```

### [Timers](http://micrometer.io/docs/concepts#_timers)

Timers report short-duration latencies and the frequency of such events.
This is useful for tracking http calls for instance.

```clojure
(require '[macrometer.timers :as t])

(t/deftimer a-timer
  :tags {:a "a" :b "b"}
  :description "A timer for something")

;(def a-timer   (t/timer "a.timer" 
;                        :tags {:a "a" :b "b"}
;                        :description "A timer for something")) 
  
(t/dorecord a-timer (Thread/sleep 100))
(t/total-time a-timer :milliseconds)
; => 100.263522
```

There are 4 ways to use a timer:

  * Directly recoding a duration:
  
    ```clojure
    (t/record a-timer 3 :seconds)
    ```
  
  * Registering a monitored function:

    ```clojure
    (monitor t (fn [x] (Thread/sleep 100) x))
    ```

  * Monitoring a block a code:

    ```clojure
    (dorecord t (Thread/sleep 100) true)
    ```

  * Using start/stop:
  
    ```clojure
    (let [sample (t/start)]
      (Thread/sleep 100)
      (t/stop sample a-timer))
    ```

## License

Copyright © 2019-2023 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
