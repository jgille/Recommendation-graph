package recng.index;

import java.util.regex.Pattern;

/**
 * Stores an id as an int, saving memory by saving IDs such as "36287991" as a
 * wrapped int instead of a String.
 *
 * @author jon
 */
public class IntID implements ID<String> {

    private final int id;

    private IntID(int id) {
        this.id = id;
    }

    public String getID() {
        return id + "";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null || other.getClass() != getClass())
            return false;
        IntID key = (IntID) other;
        return id == key.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public static class Parser implements IDPattern<String> {

        private static final Parser INSTANCE = new Parser();
        // One non zero digit followed by 0-8 digits
        private static final Pattern PATTERN = Pattern.compile("[1-9]\\d{0,8}");

        public static IDPattern<String> getInstance() {
            return INSTANCE;
        }

        public boolean matches(String id) {
            return PATTERN.matcher(id).matches();
        }

        public ID<String> parse(String id) {
            if (!matches(id))
                throw new IDFormatException(String.format("Invalid key: %s", id));
            return new IntID(Integer.parseInt(id));
        }
    }
}
