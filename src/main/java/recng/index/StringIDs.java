package recng.index;

import java.util.Arrays;
import java.util.List;

/**
 * A class used to parse a String ID into a matching {@link ID}.
 *
 * @author jon
 */
public class StringIDs {

    @SuppressWarnings("unchecked")
    private static final List<IDPattern<String>> PARSERS =
        Arrays.asList(IntID.Parser.getInstance(),
                      LongID.Parser.getInstance(),
                      PrefixIntSuffixID.Parser.getInstance(),
                      PrefixLongSuffixID.Parser.getInstance(),
                      UTF8StringID.Parser.getInstance());

    private StringIDs() {}

    public static ID<String> parseID(String id) {
        for(IDPattern<String> parser : PARSERS) {
            if (parser.matches(id))
                return parser.parse(id);
        }
        throw new IllegalArgumentException(String.format("Unable to parse key: %s", id));
    }
}
