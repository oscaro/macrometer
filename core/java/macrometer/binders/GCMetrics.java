package macrometer.binders;

import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Nicolas Estrada.
 */
public class GCMetrics implements MeterBinder {

    private final List<GarbageCollectorMXBean> gcBeans;

    public GCMetrics() {
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {

        for (final GarbageCollectorMXBean gcBean : gcBeans) {
            if (gcBean.getCollectionCount() > 0L && gcBean.getCollectionTime() > 0L) {
                //noinspection ObjectAllocationInLoop
                FunctionTimer
                  .builder("jvm.gc.collection",
                    gcBean,
                    GarbageCollectorMXBean::getCollectionCount,
                    GarbageCollectorMXBean::getCollectionTime,
                    MILLISECONDS)
                  .description("The total number of collections that have occurred and the approximate elapsed time")
                  .tag("gc", gcBean.getName())
                  .register(registry);

            }
        }
    }
}
