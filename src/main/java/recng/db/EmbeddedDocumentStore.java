package recng.db;

import java.io.IOException;

import recng.common.io.CSVDialect;

public interface EmbeddedDocumentStore<K> extends DocumentStore<K> {

    int importCSV(String file, CSVDialect dialect) throws IOException;
}
