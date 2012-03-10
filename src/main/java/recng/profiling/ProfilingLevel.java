package recng.profiling;

/**
 * Specifies the level of profiling to use.
 *
 * @author jon
 *
 */
public enum ProfilingLevel {

    /** No profiling */
    OFF,
    /** Profile slow requests. */
    SLOW,
    /** Profile all requests. */
    ALL;
}
