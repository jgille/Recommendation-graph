package recng.db.h2;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.h2.jdbcx.JdbcConnectionPool;

import recng.common.FieldMetadata;
import recng.common.FieldType;
import recng.common.Marshaller;
import recng.common.TableMetadata;
import recng.common.TableMetadataUtils;
import recng.common.io.CSVDialect;
import recng.db.EmbeddedDocumentStore;

/**
 * A document store backed by a H2 sql database.
 * 
 * All properties are stored as Strings in the db and conversions are made using
 * the {@link Marshaller} linked to each field in the {@link TableMetadata} for
 * this key/value store.
 * 
 * @author jon
 * 
 */
public class H2KVStore implements EmbeddedDocumentStore<String> {

    private String url, table, user, pwd, primaryKey;
    private final TableMetadata tableMetadata;
    private JdbcConnectionPool pool;

    /**
     * Initiates a h2 store.
     * 
     * @param tableMetadata
     *            Describes the entries in this key/value store.
     */
    public H2KVStore(TableMetadata tableMetadata) {
        this.tableMetadata = tableMetadata;
    }

    /**
     * Initiates a connection to the db.
     * 
     * Expected properties: url - The database url or directory table - The
     * table name user - The database user pwd - The password primary_key - The
     * field used as primary key
     */
    @Override
    public void init(Map<String, String> properties) {
        try {
            // Load H2 driver
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.url = String.format("jdbc:h2:%s", properties.get("url"));
        this.table = properties.get("table");
        this.user = properties.get("user");
        this.pwd = properties.get("pwd");
        this.primaryKey = properties.get("primary_key");
        this.pool = JdbcConnectionPool.create(url, user, pwd);
    }

    @Override
    public Map<String, Object> get(String key) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> res = null;
        try {
            con = getConnection();
            String query =
                String.format("select * from %s where %s = ?", table,
                              primaryKey);
            ps = con.prepareStatement(query);
            ps.setString(1, key);
            rs = ps.executeQuery();
            if (rs.next()) {
                res = new HashMap<String, Object>();
                ResultSetMetaData metadata = rs.getMetaData();
                int columnCount = metadata.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String fieldName = metadata.getColumnLabel(i);
                    FieldMetadata fm =
                        tableMetadata.getFieldMetadata(fieldName);
                    fieldName = fm.getFieldName();
                    res.put(fieldName, getObject(rs, fm, i));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(rs);
            close(ps);
            close(con);
        }
        return res;
    }

    /**
     * Releases any resources aquired (connections etc).
     */
    public void dispose() {
        pool.dispose();
    }

    /**
     * Gets and parses a serialized String or an array of serialized Strings.
     */
    private Object getObject(ResultSet rs, FieldMetadata fm, int index)
        throws SQLException {
        if (fm.isRepeated()) {
            Array sqlArr = rs.getArray(index);
            Object[] oa = (Object[]) sqlArr.getArray();
            List<Object> values = new ArrayList<Object>();
            for (Object o : oa) {
                String s = serializeToString(o, fm);
                if (s != null)
                    values.add(fm.getMarshaller().parse(s));
            }
            return values;
        }
        String s = rs.getString(index);
        if (s == null)
            return null;
        return fm.getMarshaller().parse(s);
    }

    /**
     * Sets a prepared statemen paramater either to a serialized String or and
     * array of serialized Strings.
     */
    private void setObject(PreparedStatement ps, FieldMetadata fm, int index,
                           Object value)
        throws SQLException {
        if (fm.isRepeated()) {
            @SuppressWarnings("unchecked")
            List<Object> l = (List<Object>) value;
            Object[] arr = new Object[l.size()];
            int i = 0;
            for (Object o : l)
                arr[i++] = serializeToString(o, fm);
            ps.setObject(index, arr);
        } else {
            ps.setString(index, serializeToString(value, fm));
        }
    }

    /**
     * All values are stored as Strings, serialized using this method.
     */
    private String serializeToString(Object value, FieldMetadata fm) {
        if (value == null)
            return null;
        return fm.getMarshaller().serializeToString(value);
    }

    /**
     * Builds sql that merges (updates or inserts) a row.
     */
    private String getMergeSQL(Map<String, Object> properties) {
        List<String> fields = new ArrayList<String>(properties.keySet());
        StringBuilder sql =
            new StringBuilder("merge into ").append(table).append(" (");
        boolean first = true;
        StringBuilder params = new StringBuilder();
        for (String field : fields) {
            if (!first) {
                sql.append(", ");
                params.append(", ");
            }
            sql.append(field);
            params.append("?");
            first = false;
        }
        sql.append(") key(" + primaryKey + ") values (").append(params)
            .append(")");
        return sql.toString();
    }

    @Override
    public void put(String key, Map<String, Object> value) {
        Connection con = null;
        PreparedStatement ps = null;
        List<String> fields = new ArrayList<String>(value.keySet());
        String sql = getMergeSQL(value);
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            int i = 1;
            for (String field : fields) {
                Object fieldValue = value.get(field);
                setObject(ps, tableMetadata.getFieldMetadata(field),
                          i++, fieldValue);
            }
            ps.execute();
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(ps);
            close(con);
        }
    }

    @Override
    public Map<String, Object> remove(String key) {
        Map<String, Object> res = get(key);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps =
                con.prepareStatement(String
                    .format("delete from %s where %s = ?", table, primaryKey));
            ps.setString(1, key);
            ps.execute();
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(ps);
            close(con);
        }
        return res;
    }

    private Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    /**
     * Creates the configured database table if it does not already exist.
     */
    public void createTableIfNotExists() {
        List<String> fields = tableMetadata.getFields();
        StringBuilder sql =
            new StringBuilder("create table if not exists ")
                .append(table).append(" (").append(primaryKey)
                .append(" VARCHAR(500) primary key ");
        for (String field : fields) {
            if (field.toUpperCase().equals(primaryKey.toUpperCase()))
                continue;
            FieldMetadata fm = tableMetadata.getFieldMetadata(field);
            String sqlType = getSQLType(fm);
            sql.append(", ").append(field.toUpperCase()).append(" ")
                .append(sqlType).append(" ");
        }
        sql.append(")");
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql.toString());
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(ps);
            close(con);
        }
    }

    /**
     * Drops the configured table if it exists, otherwise does nothing.
     */
    private void dropTableIfExists() {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps =
                con.prepareStatement(String.format("drop table if exists %s",
                                                   table));
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(ps);
            close(con);
        }
    }

    /**
     * Fields are either Strings or arrays of Strings
     */
    private String getSQLType(FieldMetadata fm) {
        if (fm.isRepeated())
            return "ARRAY";
        if (fm.getType() == FieldType.STRING)
            return "VARCHAR(20000)";
        return "VARCHAR(32)";
    }

    /**
     * Imports data from a csv file using the built in function csvread.
     * 
     * See: http://www.h2database.com/html/functions.html#csvread
     * 
     * NOTE: Will drop all previously stored data!
     */
    @Override
    public int importCSV(String file, CSVDialect dialect) {
        // Drop the table if necessary
        dropTableIfExists();
        StringBuilder sql =
            new StringBuilder("create table ").append(table).append(" (")
                .append(primaryKey.toUpperCase())
                .append(" VARCHAR(512) primary key ");
        StringBuilder header = new StringBuilder();
        boolean first = true;
        for (String field : tableMetadata.getFields()) {
            if (!first)
                header.append(dialect.getSeparator());
            header.append(field.toUpperCase());
            first = false;
            if (field.toUpperCase().equals(primaryKey.toUpperCase()))
                continue;
            FieldMetadata fm = tableMetadata.getFieldMetadata(field);
            String sqlType = getSQLType(fm);
            sql.append(", ").append(field.toUpperCase()).append(" ")
                .append(sqlType).append(" ");
        }
        String csvRead =
            String
                .format("csvread('%s', '%s', 'charset=UTF-8 fieldSeparator=%s escape=%s')",
                        file, header.toString(), dialect.getSeparator(),
                        dialect.getEscapeChar());
        sql.append(") as select * from ").append(csvRead);
        Connection con = null;
        PreparedStatement importStatement = null;
        PreparedStatement selectStatement = null;
        ResultSet rs = null;
        int inserted = 0;
        try {
            con = getConnection();
            importStatement = con.prepareStatement(sql.toString());
            importStatement.executeUpdate();
            selectStatement =
                con.prepareStatement(String.format("select count(*) from %s",
                                                   table));
            rs = selectStatement.executeQuery();
            if (rs.next())
                inserted = rs.getInt(1);
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(rs);
            close(importStatement);
            close(selectStatement);
            close(con);
        }
        return inserted;
    }

    private static void close(Connection con) {
        if (con == null)
            return;
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void close(Statement st) {
        if (st == null)
            return;
        try {
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void close(ResultSet rs) {
        if (rs == null)
            return;
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Imports data from a csv file.
     * 
     * Expected parameters: db url (path), table name, data file, format file
     */
    public static void main(String[] args) throws SQLException, IOException {
        int i = 0;
        String url = args[i++];
        String table = args[i++];
        String user = "test";
        String pwd = "";
        String primaryKey = FieldMetadata.ID.getFieldName();
        String dataFile = args[i++];
        String formatFile = args[i++];
        TableMetadata metadata =
            TableMetadataUtils.parseTableMetadata(formatFile);
        H2KVStore store = new H2KVStore(metadata);
        Map<String, String> config = new HashMap<String, String>();
        config.put("url", url);
        config.put("table", table);
        config.put("user", user);
        config.put("pwd", pwd);
        config.put("primary_key", primaryKey);
        store.init(config);
        System.out.println("Importing product data...");
        long t0 = System.currentTimeMillis();
        int inserted = 0;
        try {
            inserted = store.importCSV(dataFile, new CSVDialect());
        } finally {
            store.dispose();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(String.format("Inserted %s rows in %s ms.",
                                         inserted, t1 - t0));
    }
}
