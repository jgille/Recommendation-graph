package recng.profiling;

/**
 * Specifies the profiling level and slow limit to use when profiling.
 *
 * @author jon
 *
 */
public class ProfilerSettings {

    public static final ProfilerSettings DEFAULT_SETTINGS =
        new ProfilerSettings(ProfilingLevel.OFF, 100);

    private final ProfilingLevel level;
    private final int slowMillis;

    /**
     * Constructs a new settings instance.
     *
     * @param level
     *            The profiling level to use.
     * @param slowMillis
     *            The lower time limit for what is regarded as a slow request.
     */
    public ProfilerSettings(ProfilingLevel level, int slowMillis) {
        this.level = level;
        this.slowMillis = slowMillis;
    }

    public ProfilingLevel getLevel() {
        return level;
    }

    public int getSlowMillis() {
        return slowMillis;
    }

    @Override
    public String toString() {
        return "ProfilerSettings [level=" + level + ", slowMillis=" + slowMillis + "]";
    }
}
