package recng.recommendations;

import recng.index.ID;
import recng.index.StringIDs;

public class StringIDFactory implements IDFactory<ID<String>> {

    @Override
    public ID<String> fromString(String id) {
        return StringIDs.parseKey(id);
    }

    @Override
    public String toString(ID<String> id) {
        return id.getID();
    }

}
