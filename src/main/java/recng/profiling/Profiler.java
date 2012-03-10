package recng.profiling;

import java.util.Iterator;

/**
 * Classes that may profile requests should implement this interface.
 *
 * @author jon
 *
 */
public interface Profiler {

    void setProfilerSettings(ProfilerSettings settings);

    ProfilerSettings getProfilerSettings();

    Iterator<ProfilerEntry> getProfilerEntries();

}
