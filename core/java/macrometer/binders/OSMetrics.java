package macrometer.binders;

import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static java.lang.Float.parseFloat;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Paths.get;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;
import static macrometer.binders.RuntimeMetrics.BYTES;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Nicolas Estrada.
 */
public class OSMetrics implements MeterBinder {

    private static final Logger log = getLogger(OSMetrics.class);
    private static final Pattern WHITESPACE = compile("\\s+");

    private final OperatingSystemMXBean osBean;
    private final boolean linux;

    private final AtomicLong virtualMemory = new AtomicLong();
    private final AtomicLong residentMemory = new AtomicLong();

    public OSMetrics() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.linux = osBean.getName().indexOf("Linux") == 0;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        registerProcessMeters(registry);
        registerPhysicalMemoryMeters(registry);
        if (linux) {
            registerMemoryMetersLinux(registry);
        }
    }

    private void registerProcessMeters(final MeterRegistry registry) {

        if (osBean.getCommittedVirtualMemorySize() > 0L) {
            Gauge
              .builder("committed.virtual.memory", osBean, OperatingSystemMXBean::getCommittedVirtualMemorySize)
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
    }

    private void registerPhysicalMemoryMeters(final MeterRegistry registry) {

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

    private void registerMemoryMetersLinux(final MeterRegistry registry) {

        Gauge
          .builder("process.virtual.memory", virtualMemory, AtomicLong::doubleValue)
          .description("Current virtual memory usage")
          .baseUnit(BYTES)
          .register(registry);

        Gauge
          .builder("process.resident.memory", residentMemory, AtomicLong::doubleValue)
          .description("Resident set size")
          .baseUnit(BYTES)
          .register(registry);

        final ThreadFactory tFactory = r -> {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        };

        pollProcSelfStatus();
        newSingleThreadScheduledExecutor(tFactory)
          .scheduleAtFixedRate(this::pollProcSelfStatus, 10, 10, SECONDS);

    }

    private void pollProcSelfStatus() {
        //noinspection HardcodedFileSeparator
        try (final BufferedReader reader = newBufferedReader(get("/proc/self/status"), US_ASCII)) {
            reader
              .lines()
              .forEach(s -> {
                  if (s.startsWith("VmSize:")) {
                      virtualMemory.set(readBytes(s));
                  } else if (s.startsWith("VmRSS:")) {
                      residentMemory.set(readBytes(s));
                  }
              });
        } catch (final IOException e) {
            log.debug(e.getMessage());
        }
    }

    private static long readBytes(final CharSequence line) {
        final float kilobytes = parseFloat(WHITESPACE.split(line)[1]);
        //noinspection NumericCastThatLosesPrecision
        return (long) kilobytes << 10;
    }
}
