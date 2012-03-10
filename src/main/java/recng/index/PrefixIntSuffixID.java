package recng.index;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores an id as an int together with a prefix and/or suffix as a shared
 * String, thereby saving memory compared to storing it as a regular String.
 * Uses a flyweight pattern where common prefixes/suffixes are shared between
 * instances.
 *
 * @author jon
 */
public class PrefixIntSuffixID extends PrefixSuffixID {

    private final int root;
    private final int hc;

    private PrefixIntSuffixID(String prefix, int root, String suffix) {
        super(prefix, suffix);
        this.root = root;
        this.hc = computeHashCode();
    }

    public String getID() {
        return getPrefix() + root + getSuffix();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null || other.getClass() != getClass())
            return false;
        PrefixIntSuffixID key = (PrefixIntSuffixID) other;
        if (root != key.root)
            return false;
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return hc;
    }

    private int computeHashCode() {
        return 7 * (11 * root + super.hashCode());
    }

    public static class Parser implements IDPattern<String> {

        private static final Parser INSTANCE = new Parser();
        private static final Pattern PATTERN = Pattern
            .compile("(\\w{0,3}?)([1-9]\\d{0,8})(\\D{0,3})");

        public static IDPattern<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public ID<String> parse(String id) {
            Matcher m = PATTERN.matcher(id);
            if (!m.matches())
                throw new IDFormatException(String.format("Invalid key: %s", id));
            String prefix = m.group(1);
            String intVal = m.group(2);
            String suffix = m.group(3);
            return new PrefixIntSuffixID(prefix,
                                         Integer.parseInt(intVal),
                                         suffix);
        }
    }
}
