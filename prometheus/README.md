# macrometer-prometheus

A integrant/pedestal component for exporting prometheus metrics.

## Usage

It's a integrant component exposing a `/metrics` route.
Look at `test/macrometer/prometheus_test.clj`

### Configuration

```clojure
{:component/metrics {:route "/metrics"
                     :global? true
                     :binders {:hotspot? true
                               :logging? true
                               :kafka?   true}}}
```
                     
##### Attributes

| key                    | type      | default      | description
|------------------------|-----------|--------------|-----------------------
| `:route`               | `string`  | `"/metrics"` | Path for the prometheus endpoint 
| `:global?`             | `boolean` | `true`       | Add to the global (ie. default) registry
| `[:binders :hotspot?`] | `boolean` | `false`      | If `true`, include default hotspot metrics
| `[:binders :logging?`] | `boolean` | `false`      | If `true`, include logback event metrics
| `[:binders :kafka?`]   | `boolean` | `false`      | If `true`, include kafka consumer metrics

## License

Copyright © 2019 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
