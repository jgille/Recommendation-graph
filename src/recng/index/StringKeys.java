package recng.index;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked") 
public class StringKeys {

    private static final List<KeyFactory<String>> FACTORIES =
        Arrays.asList(IntKey.Factory.getInstance(),
                      LongKey.Factory.getInstance(),
                      PrefixIntSuffixKey.Factory.getInstance(),
                      PrefixLongSuffixKey.Factory.getInstance(),
                      UTF8StringKey.Factory.getInstance());

    private StringKeys() {}

    public static Key<String> parseKey(String id) {
        for(KeyFactory<String> factory : FACTORIES) {
            if (factory.matches(id))
                return factory.parse(id);
        }
        throw new IllegalArgumentException(String.format("Unable to parse key: %s", id));
    }
}
