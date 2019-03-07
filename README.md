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

## License

Copyright © 2019 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
