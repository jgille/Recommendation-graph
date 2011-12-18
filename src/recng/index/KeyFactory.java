package recng.index;

public interface KeyFactory<K> {

    boolean matches(String id);

    Key<K> parse(String id) throws Key.KeyFormatException;

}
