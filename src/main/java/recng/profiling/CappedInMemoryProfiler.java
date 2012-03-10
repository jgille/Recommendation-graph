package recng.profiling;

import java.util.Iterator;

import recng.common.CappedArray;
import recng.jmx.JMXUtils;
import recng.profiling.jmx.JMXProfiler;

/**
 * A profiler that stores profiled requests in memory only in a capped (fixed
 * size) array.
 *
 * This class is thread safe.
 *
 * @author jon
 *
 */
public class CappedInMemoryProfiler extends AbstractProfiler {

    private final CappedArray<ProfilerEntry> profiled;

    public CappedInMemoryProfiler(int maxEntries) {
        this.profiled = new CappedArray<ProfilerEntry>(maxEntries);
        JMXUtils.registerMBean(new JMXProfiler(this));
    }

    @Override
    public Iterator<ProfilerEntry> getProfilerEntries() {
        return profiled.asList().iterator();
    }

    @Override
    public void logProfilerEntry(ProfilerEntry entry) {
        ProfilerSettings settings = getProfilerSettings();
        switch (settings.getLevel()) {
        case OFF:
            break;
        case SLOW:
            if (entry.getMillis() > settings.getSlowMillis())
                profiled.push(entry);
            break;
        case ALL:
            profiled.push(entry);
        }
    }
}
