package recng.profiling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base profiler class.
 *
 * Subclasses are expected to be thread safe.
 *
 * @author jon
 *
 */
public abstract class AbstractProfiler implements Profiler {

    private ProfilerSettings settings;

    protected AbstractProfiler() {
        this.settings = ProfilerSettings.DEFAULT_SETTINGS;
    }

    @Override
    public synchronized void setProfilerSettings(ProfilerSettings settings) {
        this.settings = settings;
    }

    @Override
    public synchronized ProfilerSettings getProfilerSettings() {
        return settings;
    }

    /**
     * Logs a profiled request.
     */
    public abstract void logProfilerEntry(ProfilerEntry entry);

    /**
     * Convenience method for getting all profiled entries in a list.
     *
     * Note that this might cause memory issues if the number of profiled
     * entries is very large.
     */
    public List<ProfilerEntry> getProfilerEntriesAsList() {
        List<ProfilerEntry> entries = new ArrayList<ProfilerEntry>();
        for (Iterator<ProfilerEntry> it = getProfilerEntries(); it.hasNext();)
            entries.add(it.next());
        return entries;
    }

    @Override
    public String toString() {
        return "AbstractProfiler [settings=" + settings + "]";
    }
}
