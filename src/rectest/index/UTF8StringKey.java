package rectest.index;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class UTF8StringKey implements Key<String> {

    private final byte[] bytes;

    private UTF8StringKey(String id) {
        try {
            this.bytes = id.getBytes("UTF8");
        } catch(UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getValue() {
        try {
            return new String(bytes, "UTF8");
        } catch(UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override public boolean equals(Object other) {
        if(other == null || other.getClass() != getClass())
            return false;
        UTF8StringKey key = (UTF8StringKey)other;
        byte[] ba = key.bytes;
        return Arrays.equals(bytes, ba);
    }

    @Override public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override public String toString() {
        return String.format("UTF8StringKey: %s", getValue());
    }

    public static class Factory implements KeyFactory<String> {

        private static final Factory INSTANCE = new Factory();
        private static final Pattern PATTERN = Pattern.compile(".*");

        public static KeyFactory<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public Key<String> parse(String id) {
            return new UTF8StringKey(id);
        }
    }

}
