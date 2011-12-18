package recng.index;

import java.util.regex.Pattern;

public class IntKey implements Key<String> {

    private final int id;

    private IntKey(int id) {
        this.id = id;
    }

    public String getValue() {
        return id + "";
    }

    @Override public boolean equals(Object other) {
        if(other == null || other.getClass() != getClass())
            return false;
        IntKey key = (IntKey)other;
        return id == key.id;
    }

    @Override public int hashCode() {
        return id;
    }

    @Override public String toString() {
        return String.format("IntKey: %s", id);
    }

    public static class Factory implements KeyFactory<String> {

        private static final Factory INSTANCE = new Factory();
        // One non zero digit followed by 0-8 digits
        private static final Pattern PATTERN = Pattern.compile("[1-9]\\d{0,8}");

        public static KeyFactory<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public Key<String> parse(String id) {
            if (!matches(id))
                throw new KeyFormatException(String.format("Invalid key: %s", id));
            return new IntKey(Integer.parseInt(id));
        }
    }
}
