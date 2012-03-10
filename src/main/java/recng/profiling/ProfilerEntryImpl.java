package recng.profiling;

import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link ProfilerEntry} implementation.
 * 
 * @author jon
 * 
 */
public class ProfilerEntryImpl implements ProfilerEntry {

    private static final long serialVersionUID = 201202181542L;

    private final String description;
    private final Map<String, Object> properties;

    private long startTimeNanos = -1;
    private long finishTimeNanos = -1;

    public ProfilerEntryImpl(String description) {
        this.description = description;
        this.properties = new HashMap<String, Object>();
    }

    @Override
    public long getTimestamp() {
        if (startTimeNanos < 0)
            throw new IllegalStateException("This entry is not started.");
        return startTimeNanos / 1000;
    }

    @Override
    public long getMillis() {
        if (startTimeNanos < 0)
            throw new IllegalStateException("This entry is not started.");
        if (finishTimeNanos < 0)
            throw new IllegalStateException("This entry is not finished.");
        return Math.round((finishTimeNanos - startTimeNanos) / 1000000d);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void start() {
        start(System.nanoTime());
    }

    public void start(long startTimeNanos) {
        if (this.startTimeNanos > -1)
            throw new IllegalStateException("This entry has already been started.");
        this.startTimeNanos = startTimeNanos;
    }

    @Override
    public void finish() {
        finish(System.nanoTime());
    }

    public void finish(long finishTimeNanos) {
        if (this.finishTimeNanos > -1)
            throw new IllegalStateException("This entry is already finished.");
        this.finishTimeNanos = finishTimeNanos;
    }

    @Override
    public ProfilerEntry setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public long getSplitMillis() {
        if (startTimeNanos < 0)
            throw new IllegalStateException("This entry is not started.");
        if (finishTimeNanos >= 0)
            throw new IllegalStateException("This entry is already finished.");
        return Math.round((System.nanoTime() - startTimeNanos) / 1000000d);
    }

    @Override
    public String toString() {
        long millis = 0;
        if (startTimeNanos > -1)
            millis = finishTimeNanos > -1 ? getMillis() : getSplitMillis();
        Map<String, Object> map = new HashMap<String, Object>(properties);
        map.put("ts", getTimestamp());
        map.put("millis", millis);
        map.put("description", description);
        return map.toString();
    }

}
