package recng.db.mongodb;

import java.net.UnknownHostException;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import recng.db.KVStore;

/**
 * A key/value store backed by mongodb.
 *
 * @author jon
 *
 */
public class MongoKVStore implements KVStore<String, Map<String, Object>> {

    private DBCollection col = null;

    @Override
    public void init(Map<String, String> properties) {
        String url = properties.get("URL");
        String dbName = properties.get("db");
        String colName = properties.get("collection");
        try {
            Mongo con = url != null ? new Mongo(url) : new Mongo();
            DB db = con.getDB(dbName);
            col = db.getCollection(colName);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> get(String key) {
        if (col == null)
            throw new IllegalStateException("Connection not initiated");
        DBObject dbo = col.findOne(new BasicDBObject("_id", key));
        if (dbo == null)
            return null;
        return dbo.toMap();
    }

    @Override
    public void put(String key, Map<String, Object> value) {
        if (col == null)
            throw new IllegalStateException("Connection not initiated");
        DBObject dbo = new BasicDBObject(value);
        dbo.put("_id", key);
        col.save(dbo);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> remove(String key) {
        if (col == null)
            throw new IllegalStateException("Connection not initiated");
        DBObject dbo = col.findAndRemove(new BasicDBObject("_id", key));
        if (dbo == null)
            return null;
        return dbo.toMap();
    }
}
