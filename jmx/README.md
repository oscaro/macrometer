# macrometer-jmx

A integrant/pedestal component for exporting JMX metrics.

## Usage

It's a integrant component exposing a all metrics using JMX.
Look at `test/macrometer/jmx_test.clj`

### Configuration

```clojure
{:component/metrics {:prefix  "jmx"
                     :domain  "metrics"
                     :global? true
                     :binders {:hotspot? false
                               :logging? false
                               :kafka?   false}}}
```
                     
##### Attributes

| key                    | type      | default      | description
|------------------------|-----------|--------------|-----------------------
| `:domain`              | `string`  | `metrics`    | Domain of all metrics.
| `:global?`             | `boolean` | `true`       | Add to the global (ie. default) registry
| `[:binders :hotspot?`] | `boolean` | `false`      | If `true`, include default hotspot metrics
| `[:binders :logging?`] | `boolean` | `false`      | If `true`, include logback event metrics
| `[:binders :kafka?`]   | `boolean` | `false`      | If `true`, include kafka consumer metrics

## License

Copyright © 2019 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
