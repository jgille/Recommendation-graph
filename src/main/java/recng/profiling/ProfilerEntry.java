package recng.profiling;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * An entry used when profiling a request.
 *
 * @author jon
 *
 */
public interface ProfilerEntry extends Serializable {

    /**
     * Starts profiling by starting a stop watch.
     *
     * May only be used for not yet started entries.
     */
    void start();

    /**
     * Finishes profiling by stopping the stop watch.
     *
     * May only be used for started, not yet finished, entries.
     */
    void finish();

    /**
     * Gets the timestamp (time in millis) of the start time for this entry.
     */
    long getTimestamp();

    /**
     * Gets the elapsed time in milliseconds.
     * 
     * May only be used for finished entries.
     */
    long getMillis();

    /**
     * Gets a split time (from start to now) in milli seconds.
     * 
     * May only be used for started entries that are not yet finished.
     */
    long getSplitMillis();

    /**
     * Gets a description of this entry.
     */
    String getDescription();

    /**
     * Gets implementation specific properties for this entry.
     */
    Map<String, Object> getProperties();

    /**
     * Sets a property for this entry.
     */
    ProfilerEntry setProperty(String key, Object value);

}
