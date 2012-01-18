package recng.db.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import recng.recommendations.data.AbstractDataUploader;
import recng.recommendations.data.DataReader;
import recng.recommendations.data.DataRowParser;

/**
 * Uploads data from file into mongodb. Each line in the file is mapped to a
 * document in mongodb.
 *
 * NOTE: All documents are saved in a temporary collection during the upload.
 * These documents are moved to the primary collection once
 * {@link MongoDataUploader#endUpload(boolean)} is called. If the upload was
 * successful, changes are pushed to the primary collection, if not all changes
 * are ignored. If an unexpected error occurs in endUpload, the data may be in
 * an inconsistent state and it is up to the caller to solve this.
 *
 * NOTE: ALWAYS call {@link MongoDataUploader#endUpload(boolean)} in a finally
 * block.
 *
 * @author jon
 *
 */
public class MongoDataUploader extends AbstractDataUploader implements DataReader {

    private final DataRowParser parser;
    private final DB db;
    private final String collectionName;
    private final String tmpCollectionName;
    private final boolean purgeOldData;
    private final List<DBObject> buffer = new ArrayList<DBObject>();

    private static final int MAX_BUFFER_SIZE = 5000;

    /**
     * Constructs a new uploader instance.
     *
     * @param parser
     *            Used to parse lines in the uploaded file into property maps.
     * @param db
     *            The database in which to save the data.
     * @param collectionName
     *            The name of the collection in which to save the data.
     * @param purgeOldData
     *            True if all data that existed in the collection prior to the
     *            upload should be deleted after a successful upload. NOTE: When
     *            this is set to true, the collection may not contain any
     *            indexes other than the default _id index.
     */
    public MongoDataUploader(DataRowParser parser, DB db,
                             String collectionName, boolean purgeOldData) {
        this.parser = parser;
        this.db = db;
        this.collectionName = collectionName;
        this.tmpCollectionName =
            collectionName + "_" + System.currentTimeMillis();
        this.purgeOldData = purgeOldData;
    }

    @Override
    protected Map<String, Object> parse(String line) {
        return parser.parseLine(line);
    }

    @Override
    protected void save(Map<String, Object> properties) {
        buffer.add(new BasicDBObject(properties));
        if (buffer.size() > MAX_BUFFER_SIZE) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        if (buffer.isEmpty())
            return;
        db.getCollection(tmpCollectionName).insert(buffer);
        buffer.clear();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void startUpload() {
        DBObject options =
            BasicDBObjectBuilder.start("capped", true).append("size", 2 * 1024 *
                                                              1024 * 1024l)
                .get();
        db.createCollection(tmpCollectionName, options);
        if (purgeOldData) {
            DBCollection collection = db.getCollection(collectionName);
            List<DBObject> indexInfo = collection.getIndexInfo();

            if (indexInfo.isEmpty())
                return;
            if (indexInfo.size() == 1
                && "_id_".equals(indexInfo.get(0).get("name")))
                return;
            String msg =
                "Non standard indexes are not allowed when purgeOldData=true";
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected void endUpload(boolean successful) {
        flushBuffer();
        if (successful)
            commit();
        else
            rollback();
    }

    private void commit() {
        System.out.println("Committing...");
        if (purgeOldData) {
            db.getCollection(tmpCollectionName).ensureIndex("_id");
            if (db.getCollectionNames().contains(collectionName)) {
                String purging = collectionName + "_purging";
                db.getCollection(collectionName).rename(purging);
                db.getCollection(tmpCollectionName).rename(collectionName,
                                                           true);
                db.getCollection(purging).drop();
            } else {
                db.getCollection(tmpCollectionName)
                    .rename(collectionName, true);
            }
        } else {
            DBCollection collection = db.getCollection(collectionName);
            for (DBObject dbo : db.getCollection(tmpCollectionName).find()) {
                collection.save(dbo);
            }
            db.getCollection(tmpCollectionName).drop();
        }
    }

    private void rollback() {
        System.out.println("Performing rollback...");
        if (db.getCollectionNames().contains(tmpCollectionName))
            db.getCollection(tmpCollectionName).drop();
    }
}
