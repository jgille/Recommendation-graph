package recng.profiling.jmx;

import java.util.List;
import java.util.Map;

public interface JMXProfilerMBean {

    List<Map<String, String>> getEntries();

    String getProfilerClass();

    String getProfilerSettings();
}
