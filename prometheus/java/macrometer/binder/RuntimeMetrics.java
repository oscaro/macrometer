package macrometer.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@SuppressWarnings("HardCodedStringLiteral")
public class RuntimeMetrics implements MeterBinder {

    private static final String BYTES = "bytes";
    private final Runtime runtime;

    public RuntimeMetrics() {
        this.runtime = Runtime.getRuntime();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {

        Gauge
          .builder("jvm.free.memory", runtime, Runtime::freeMemory)
          .description("The amount of free memory in the Java Virtual Machine")
          .baseUnit(BYTES)
          .register(registry);

        Gauge
          .builder("jvm.total.memory", runtime, Runtime::totalMemory)
          .description("The total amount of memory in the Java virtual machine")
          .baseUnit(BYTES)
          .register(registry);

        if (runtime.totalMemory() != Long.MAX_VALUE) {
            Gauge
              .builder("jvm.max.memory", runtime, Runtime::totalMemory)
              .description("The maximum amount of memory that the Java virtual machine will attempt to use")
              .baseUnit(BYTES)
              .register(registry);
        }
    }
}
