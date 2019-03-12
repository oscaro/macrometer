# macrometer

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

##### Manual gauges

These are manipulated as if they were `atoms` but whose value will be read by the underlying registry.

```clojure
(require '[macrometer.gauges :as g])

(g/defgauge a-gauge
  :tags {:a "a" :b "b"}
  :unit "rps")
  
;(def a-gauge (g/gauge "a.gauge"
;                      :tags {:a "a" :b "b"}
;                      :unit "rps"))

(zero? @a-gauge)
; => true
(swap! a-gauge inc)
@a-gauge
; => 1.0
```

##### Reference tracking gauges
For `atom`s, `AtomicLong`s and other immutable `Number`s etc...

```clojure
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

Timers report a short duration event. This is usefule for tracking http calls for instance.

```clojure
(require '[macrometer.timers :as t])
(import [java.util.concurrent TimeUnit])
(import [io.micrometer.core.instrument.simple SimpleMeterRegistry])

(t/deftimer a-timer
  :tags {:a "a" :b "b"}
  :description "A timer for something"
  :registry (SimpleMeterRegistry.))

;(def a-counter (t/timer "a.counter" 
;                        :tags {:a "a" :b "b"}
;                        :description "A timer for something")) 
  
(t/monitor a-timer (Thread/sleep 100))
(.totalTime a-timer TimeUnit/MILLISECONDS)
; => 100.263522
```

There are 4 ways to use a timer:

  * Directly recoding a duration:
  
    ```clojure
    (.record a-timer 3000 TimeUnit/MILLISECONDS)
    ```
  
  * Registering a monitored function:

    ```clojure
    (monitored t (fn [x] (Thread/sleep 100) x))
    ```
  
  You can use wrapped instead of monitored for a 0-arity function.

  * Monitoring a block a code:

    ```clojure
    (monitor t (Thread/sleep 100) true)
    ```

  * Using start/stop:
  
    ```clojure
    (let [sample (t/start registry)]
      (Thread/sleep 100)
      (t/stop sample a-timer))
    ```
    
    We specify the timer only at the end to be able to set tags based on the result of the body.

## License

Copyright © 2019 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
