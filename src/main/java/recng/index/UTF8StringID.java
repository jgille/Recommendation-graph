package recng.index;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Stores a String ID as an UTF8 byte array.
 *
 * @author jon
 */
public class UTF8StringID implements ID<String> {

    private final byte[] bytes;
    private final int hc;

    private UTF8StringID(String id) {
        try {
            this.bytes = id.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        this.hc = computeHashCode();
    }

    public String getID() {
        try {
            return new String(bytes, "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null || other.getClass() != getClass())
            return false;
        UTF8StringID key = (UTF8StringID) other;
        byte[] ba = key.bytes;
        return Arrays.equals(bytes, ba);
    }

    @Override
    public int hashCode() {
        return hc;
    }

    private int computeHashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return getID();
    }

    public static class Parser implements IDPattern<String> {

        private static final Parser INSTANCE = new Parser();

        public static IDPattern<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return id != null;
        }

        public ID<String> parse(String id) {
            return new UTF8StringID(id);
        }
    }

}
