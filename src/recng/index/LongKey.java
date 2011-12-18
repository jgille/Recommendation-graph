package recng.index;

import java.util.regex.Pattern;

public class LongKey implements Key<String> {

    private final long id;

    private LongKey(long id) {
        this.id = id;
    }

    public String getValue() {
        return id + "";
    }

    @Override public boolean equals(Object other) {
        if(other == null || other.getClass() != getClass())
            return false;
        LongKey key = (LongKey)other;
        return id == key.id;
    }

    @Override public int hashCode() {
        return new Long(id).hashCode();
    }

    @Override public String toString() {
        return String.format("LongKey: %s", id);
    }

    public static class Factory implements KeyFactory<String> {

        private static final Factory INSTANCE = new Factory();
        // One non zero digit followed by 9-18 digits
        private static final Pattern PATTERN = Pattern
            .compile("[1-9]\\d{9,18}");

        public static KeyFactory<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public Key<String> parse(String id) {
            if (!matches(id))
                throw new KeyFormatException(String.format("Invalid key: %s", id));
            return new LongKey(Long.parseLong(id));
        }
    }
}
