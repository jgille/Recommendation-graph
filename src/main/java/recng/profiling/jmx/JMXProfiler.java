package recng.profiling.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import recng.profiling.Profiler;
import recng.profiling.ProfilerEntry;
import recng.profiling.ProfilerSettings;

public class JMXProfiler implements JMXProfilerMBean {

    private final Profiler profiler;

    public JMXProfiler(Profiler profiler) {
        this.profiler = profiler;
    }

    @Override
    public List<Map<String, String>> getEntries() {
        List<Map<String, String>> entries = new ArrayList<Map<String, String>>();
        for (Iterator<ProfilerEntry> pEntries = profiler.getProfilerEntries(); pEntries.hasNext();) {
            ProfilerEntry pEntry = pEntries.next();
            Map<String, Object> properties = pEntry.getProperties();
            Map<String, String> stringProps = new HashMap<String, String>();
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                stringProps.put(e.getKey(), "" + e.getValue());
            }
            stringProps.put("description", pEntry.getDescription());
            entries.add(stringProps);
        }
        return entries;
    }

    @Override
    public String getProfilerClass() {
        return profiler.getClass().getName();
    }

    @Override
    public String getProfilerSettings() {
        ProfilerSettings settings = profiler.getProfilerSettings();
        return settings.toString();
    }
}
