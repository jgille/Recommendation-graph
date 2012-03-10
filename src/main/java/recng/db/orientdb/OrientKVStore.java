package recng.db.orientdb;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import recng.common.BinPropertyContainer;
import recng.common.FieldMetadata;
import recng.common.PropertyContainer;
import recng.common.PropertyContainerFactory;
import recng.common.TableMetadata;
import recng.common.TableMetadataUtils;
import recng.common.io.CSVDescriptor;
import recng.common.io.CSVDialect;
import recng.common.io.CSVPropertyCursor;
import recng.common.io.CSVUtils;
import recng.db.EmbeddedDocumentStore;

/**
 * A document store backed by an embedded OrientDB document database.
 * 
 * @author jon
 * 
 */
public class OrientKVStore implements EmbeddedDocumentStore<String> {

    private String url, className, user, pwd, primaryKey;

    private final TableMetadata metadata;

    public OrientKVStore(TableMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void init(Map<String, String> properties) {
        this.url = String.format("local:%s", properties.get("url"));
        this.className = properties.get("oClass");
        this.user = properties.get("user");
        this.pwd = properties.get("pwd");
        this.primaryKey = properties.get("primary_key");
    }

    @Override
    public Map<String, Object> get(String key) {
        String sql = String.format("select * from %s where %s = ?",
                                   className, primaryKey);
        OSQLQuery<ODocument> query = new OSQLSynchQuery<ODocument>(sql);
        ODatabaseDocument db = getDB();
        Map<String, Object> props = null;
        try {
            List<ODocument> docs = db.query(query, key);
            if (docs != null && !docs.isEmpty()) {
                ODocument doc = docs.get(0);
                props = new HashMap<String, Object>();
                for (String field : doc.fieldNames())
                    props.put(field, doc.field(field));
            }
        } finally {
            db.close();
        }
        return props;
    }

    @Override
    public void put(String key, Map<String, Object> value) {
        Map<String, Object> properties = new HashMap<String, Object>(value);
        properties.put(primaryKey, key);
        ODatabaseDocument db = getDB();
        try {
            ODocument doc = db.newInstance(className);
            doc.merge(properties, false, false);
            db.save(doc);
        } finally {
            db.close();
        }
    }

    @Override
    public Map<String, Object> remove(String key) {
        Map<String, Object> prev = get(key);
        String sql = String.format("delete from %s where %s = ?",
                                   className, primaryKey);
        ODatabaseDocument db = getDB();
        try {
            db.command(new OCommandSQL(sql)).execute(key);
        } finally {
            db.close();
        }
        return prev;
    }

    private ODatabaseDocument getDB() {
        return new ODatabaseDocumentPool().acquire(url, user, pwd);
    }

    /**
     * Creates the schema for this document store.
     * 
     */
    public void setupSchema() {
        ODatabaseDocument db = new ODatabaseDocumentTx(url).create();
        try {
            OSchema schema = db.getMetadata().getSchema();
            OClass oClass = schema.createClass(className);
            oClass.createProperty(primaryKey, OType.STRING);
            schema.save();
            String sql =
                String.format("create index %s.%s unique",
                              className, primaryKey);
            db.command(new OCommandSQL(sql)).execute();
        } finally {
            db.close();
        }
    }

    /**
     * Imports data from a csv file.
     * 
     * NOTE: Will drop all previously stored data!
     */
    @Override
    public int importCSV(String file, CSVDialect dialect) throws IOException {
        CSVDescriptor descriptor = new CSVDescriptor();
        descriptor.setGzipped(file.endsWith(".gz")).setMetadata(metadata);
        PropertyContainerFactory factory = new BinPropertyContainer.Factory();
        CSVPropertyCursor cursor =
            CSVUtils.readAndParse(file, descriptor, factory);
        int added = 0;
        try {
            ODatabaseDocument db = getDB();
            try {
                db.declareIntent(new OIntentMassiveInsert());
                PropertyContainer props;
                // Performance trick described here:
                // http://code.google.com/p/orient/wiki/PerformanceTuningDocument#Massive_Insertion
                ODocument doc = new ODocument(db);
                while ((props = cursor.nextRow()) != null) {
                    Map<String, Object> properties = props.asMap();
                    doc.reset();
                    doc.setClassName(className);
                    doc.merge(properties, false, false);
                    db.save(doc);
                    added++;
                    if (added % 25000 == 0)
                        System.out.println(String
                            .format("Added %s documents..", added));
                }
            } finally {
                db.close();
            }
        } finally {
            cursor.close();
        }
        return added;
    }

    public static void main(String[] args) throws IOException {
        int i = 0;
        String url = args[i++];
        String oClass = args[i++];
        String user = "admin";
        String pwd = "admin";
        String primaryKey = FieldMetadata.ID.getFieldName();
        String productFile = args[i++];
        String productFormatFile = args[i++];
        TableMetadata metadata =
            TableMetadataUtils.parseTableMetadata(productFormatFile);
        OrientKVStore store = new OrientKVStore(metadata);
        Map<String, String> config = new HashMap<String, String>();
        config.put("url", url);
        config.put("oClass", oClass);
        config.put("user", user);
        config.put("pwd", pwd);
        config.put("primary_key", primaryKey);
        store.init(config);
        store.setupSchema();
        System.out.println("Importing product data...");
        long t0 = System.currentTimeMillis();
        int inserted = store.importCSV(productFile, new CSVDialect());
        long t1 = System.currentTimeMillis();
        System.out.println(String.format("Inserted %s rows in %s ms.",
                                         inserted, t1 - t0));
    }

}
