package recng.index;

import java.util.regex.Pattern;

/**
 * Stores an id as a long, saving memory by saving IDs such as "362879915893020"
 * as a wrapped long instead of a String.
 * 
 * @author jon
 */
public class LongID implements ID<String> {

    private final long id;

    private LongID(long id) {
        this.id = id;
    }

    public String getID() {
        return id + "";
    }

    @Override public boolean equals(Object other) {
        if(other == null || other.getClass() != getClass())
            return false;
        LongID key = (LongID)other;
        return id == key.id;
    }

    @Override public int hashCode() {
        return new Long(id).hashCode();
    }

    @Override public String toString() {
        return String.format("LongKey: %s", id);
    }

    public static class Parser implements IDPattern<String> {

        private static final Parser INSTANCE = new Parser();
        // One non zero digit followed by 9-18 digits
        private static final Pattern PATTERN = Pattern
            .compile("[1-9]\\d{9,18}");

        public static IDPattern<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public ID<String> parse(String id) {
            if (!matches(id))
                throw new IDFormatException(String.format("Invalid key: %s", id));
            return new LongID(Long.parseLong(id));
        }
    }
}
