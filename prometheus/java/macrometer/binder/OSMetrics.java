package macrometer.binder;

import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.lang.management.ManagementFactory;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static macrometer.binder.RuntimeMetrics.BYTES;

@SuppressWarnings("HardCodedStringLiteral")
public class OSMetrics implements MeterBinder {

    private final OperatingSystemMXBean osBean;

    public OSMetrics() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {

        if (osBean.getCommittedVirtualMemorySize() > 0L) {
            Gauge
              .builder("process.virtual.memory", osBean, OperatingSystemMXBean::getCommittedVirtualMemorySize)
              .description("The amount of virtual memory that is guaranteed to be available to the running process")
              .baseUnit(BYTES)
              .register(registry);
        }

        Gauge
          .builder("total.swap.space", osBean, OperatingSystemMXBean::getTotalSwapSpaceSize)
          .description("The total amount of swap space")
          .baseUnit(BYTES)
          .register(registry);

        Gauge
          .builder("free.swap.space", osBean, OperatingSystemMXBean::getFreeSwapSpaceSize)
          .description("The amount of free swap space")
          .baseUnit(BYTES)
          .register(registry);

        if (osBean.getProcessCpuTime() > 0L) {
            FunctionCounter
              .builder("process.cpu.time", osBean, bean -> NANOSECONDS.toSeconds(bean.getProcessCpuTime()))
              .description("the CPU time used by the process on which the Java virtual machine is running")
              .baseUnit("seconds")
              .register(registry);
        }

        Gauge
          .builder("free.physical.memory", osBean, OperatingSystemMXBean::getFreePhysicalMemorySize)
          .description("The amount of free physical memory")
          .baseUnit(BYTES)
          .register(registry);

        Gauge
          .builder("total.physical.memory", osBean, OperatingSystemMXBean::getTotalPhysicalMemorySize)
          .description("The total amount of physical memory")
          .baseUnit(BYTES)
          .register(registry);
    }
}
