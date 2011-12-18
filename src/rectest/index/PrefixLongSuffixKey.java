package rectest.index;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixLongSuffixKey extends PrefixSuffixKey implements Key<String> {

    private final long root;

    private PrefixLongSuffixKey(String prefix, long root, String suffix) {
        super(prefix, suffix);
        this.root = root;
    }

    public String getValue() {
        return getPrefix() + root + getSuffix();
    }

    @Override public boolean equals(Object other) {
        if(other == null || other.getClass() != getClass())
            return false;
        PrefixLongSuffixKey key = (PrefixLongSuffixKey)other;
        if (root != key.root)
            return false;
        return super.equals(other);
    }

    @Override public int hashCode() {
        return 7 * (11 * new Long(root).hashCode() + super.hashCode());
    }

    @Override public String toString() {
        return String.format("PrefixLongSuffixKey: p:%s, i:%s, s:%s", getPrefix(), root,
                             getSuffix());
    }

    public static class Factory implements KeyFactory<String> {

        private static final Factory INSTANCE = new Factory();
        private static final Pattern PATTERN = Pattern
            .compile("(.{0,3})([1-9]\\d{9,18})(.{0,3})");

        public static KeyFactory<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public Key<String> parse(String id) {
            Matcher m = PATTERN.matcher(id);
            if (!m.matches())
                throw new KeyFormatException(String.format("Invalid key: %s", id));
            return new PrefixLongSuffixKey(m.group(1),
                                                Long.parseLong(m.group(2)),
                                                m.group(3));
        }
    }
}
