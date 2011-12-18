package recng.index;

public interface Key<K> {

    K getValue();

    public static class KeyFormatException extends IllegalArgumentException {
        public KeyFormatException(String msg) {
            super(msg);
        }
    }
}
