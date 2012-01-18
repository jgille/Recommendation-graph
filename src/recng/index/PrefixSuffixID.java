package recng.index;

import recng.cache.Cache;
import recng.cache.CacheBuilder;

/**
 * Base class for storing IDs with common prefixes/suffixes.
 *
 * Uses a flyweight pattern where common prefixes/suffixes are shared between
 * instances.
 *
 * @author jon
 */
abstract class PrefixSuffixID {

    private static final Cache<String, String> STRING_CACHE =
        new CacheBuilder<String, String>().maxSize(1000).build();

    private final String prefix, suffix;

    protected PrefixSuffixID(String prefix, String suffix) {
        this.prefix = getOrCacheString(prefix);
        this.suffix = getOrCacheString(suffix);
    }

    private static String getOrCacheString(String s) {
        if (s == null)
            return null;
        if(STRING_CACHE.contains(s))
            return STRING_CACHE.get(s);
        STRING_CACHE.cache(s, s);
        return s;
    }

    protected String getPrefix() {
        return prefix == null ? "" : prefix;
    }

    protected String getSuffix() {
        return suffix == null ? "" : suffix;
    }

    @Override public boolean equals(Object other) {
        if(other == null || other.getClass() != getClass())
            return false;
        PrefixSuffixID pid = (PrefixSuffixID)other;
        return sameString(pid.getPrefix(), prefix) && sameString(pid.getSuffix(), suffix);
    }

    private boolean sameString(String s1, String s2) {
        if(s1 == null)
            return s2 == null;
        return s1.equals(s2);
    }

    @Override public int hashCode() {
        int hc = 0;
        if (prefix != null)
            hc += prefix.hashCode();
        if (suffix != null) {
            hc += 7 * suffix.hashCode();
            hc *= 11;
        }
        return hc;
    }

}
