package recng.recommendations;

import recng.index.ID;
import recng.index.StringIDs;

/**
 * A factory for creating {@link ID}s containing Strings.
 *
 * @author jon
 *
 */
public class StringIDParser implements IDParser<ID<String>> {

    @Override
    public ID<String> parse(String id) {
        return StringIDs.parseID(id);
    }

    @Override
    public String serialize(ID<String> id) {
        return id.getID();
    }

    @Override
    public String toString() {
        return "StringIDParser []";
    }
}
