package tests.junit.profiling;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import recng.profiling.CappedInMemoryProfiler;
import recng.profiling.ProfilerEntry;
import recng.profiling.ProfilerEntryImpl;
import recng.profiling.ProfilerSettings;
import recng.profiling.ProfilingLevel;

/**
 * Tests {@link CappedInMemoryProfiler}.
 *
 * @author jon
 *
 */
public class CappedInMemoryProfilerTest {

    @Test
    public void testEmpty() {
        CappedInMemoryProfiler profiler = new CappedInMemoryProfiler(10);
        Assert.assertEquals(Collections.emptyList(), profiler.getProfilerEntriesAsList());
    }

    @Test
    public void testOff() {
        CappedInMemoryProfiler profiler = new CappedInMemoryProfiler(10);
        Assert.assertEquals(Collections.emptyList(), profiler.getProfilerEntriesAsList());
        ProfilerEntry entry = new ProfilerEntryImpl("");
        profiler.logProfilerEntry(entry);
        // Should still be empty since the default profiling level is off.
        Assert.assertEquals(Collections.emptyList(), profiler.getProfilerEntriesAsList());
    }

    @Test
    public void testSlow() {
        CappedInMemoryProfiler profiler = new CappedInMemoryProfiler(10);
        profiler.setProfilerSettings(new ProfilerSettings(ProfilingLevel.SLOW, 10));
        Assert.assertEquals(Collections.emptyList(), profiler.getProfilerEntriesAsList());
        ProfilerEntryImpl entry1 = new ProfilerEntryImpl("entry 1");
        entry1.start(0);
        entry1.finish(20000000);
        ProfilerEntryImpl entry2 = new ProfilerEntryImpl("entry 2");
        entry2.start(0);
        entry2.finish(5000000);

        profiler.logProfilerEntry(entry1);
        profiler.logProfilerEntry(entry2);
        // One entry (the slow one) should have been logged
        List<ProfilerEntry> profiled = profiler.getProfilerEntriesAsList();
        Assert.assertEquals(1, profiled.size());
        Assert.assertEquals(profiled.get(0).getDescription(), "entry 1");
    }

    @Test
    public void testAll() {
        CappedInMemoryProfiler profiler = new CappedInMemoryProfiler(10);
        profiler.setProfilerSettings(new ProfilerSettings(ProfilingLevel.ALL, 10));
        Assert.assertEquals(Collections.emptyList(), profiler.getProfilerEntriesAsList());
        ProfilerEntryImpl entry1 = new ProfilerEntryImpl("entry 1");
        entry1.start();
        entry1.finish();
        ProfilerEntryImpl entry2 = new ProfilerEntryImpl("entry 2");
        entry2.start();
        entry2.finish();

        profiler.logProfilerEntry(entry1);
        profiler.logProfilerEntry(entry2);
        // Both entries should have been logged
        List<ProfilerEntry> profiled = profiler.getProfilerEntriesAsList();
        Assert.assertEquals(2, profiled.size());
        Assert.assertEquals(profiled.get(0).getDescription(), "entry 1");
        Assert.assertEquals(profiled.get(1).getDescription(), "entry 2");
    }

    @Test
    public void testCapped() {
        CappedInMemoryProfiler profiler = new CappedInMemoryProfiler(2);
        profiler.setProfilerSettings(new ProfilerSettings(ProfilingLevel.ALL, 10));
        Assert.assertEquals(Collections.emptyList(), profiler.getProfilerEntriesAsList());
        ProfilerEntryImpl entry1 = new ProfilerEntryImpl("entry 1");
        entry1.start();
        entry1.finish();
        ProfilerEntryImpl entry2 = new ProfilerEntryImpl("entry 2");
        entry2.start();
        entry2.finish();
        ProfilerEntryImpl entry3 = new ProfilerEntryImpl("entry 3");
        entry3.start();
        entry3.finish();

        profiler.logProfilerEntry(entry1);
        profiler.logProfilerEntry(entry2);
        profiler.logProfilerEntry(entry3);
        // All entries should have been logged, but only thw two last one should
        // remain
        List<ProfilerEntry> profiled = profiler.getProfilerEntriesAsList();
        Assert.assertEquals(2, profiled.size());
        Assert.assertEquals(profiled.get(0).getDescription(), "entry 2");
        Assert.assertEquals(profiled.get(1).getDescription(), "entry 3");
    }

}
