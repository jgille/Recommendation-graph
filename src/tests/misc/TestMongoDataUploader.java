package tests.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.DB;
import com.mongodb.Mongo;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.TableMetadata;
import recng.common.TableMetadataImpl;
import recng.db.mongodb.MongoDataUploader;
import recng.recommendations.data.CSVDataRowParser;
import recng.recommendations.data.DataRowParser;

public class TestMongoDataUploader {

    private static final Pattern METADATA_PATTERN = Pattern
        .compile("(\\S*)\\s*(\\S*)");

    public static void main(String[] args) throws IOException {
        TableMetadata metadata = parseTableMetadata(args[0]);
        String dataFile = args[1];
        DataRowParser parser = new CSVDataRowParser(metadata);
        DB db = new Mongo().getDB(args[2]);
        String collectionName = args[3];
        boolean purgeOldData = Boolean.parseBoolean(args[4]);
        MongoDataUploader uploader =
            new MongoDataUploader(parser, db, collectionName, purgeOldData);
        long t0 = System.currentTimeMillis();
        uploader.readFile(dataFile);
        long t1 = System.currentTimeMillis();
        System.out.println("Done in " + (t1 - t0) + " ms.");
    }

    // TODO: Move to util class
    public static TableMetadata parseTableMetadata(String file)
        throws IOException {
        BufferedReader br = null;
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
        fields.add(new FieldMetadataImpl("_id", FieldMetadata.Type.STRING));
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                Matcher m = METADATA_PATTERN.matcher(line);
                if (!m.matches())
                    continue;
                String fieldName = m.group(1);
                String typeName = m.group(2).toUpperCase();
                FieldMetadata.Type type = FieldMetadata.Type.valueOf(typeName);
                fields.add(createFieldMetadata(fieldName, type));
            }
            fields.add(createFieldMetadata("__is_valid",
                                           FieldMetadata.Type.BOOLEAN));
        } finally {
            if (br != null)
                br.close();
        }
        return new TableMetadataImpl(fields);
    }

    private static FieldMetadata
        createFieldMetadata(String fieldName, FieldMetadata.Type type) {
        return new FieldMetadataImpl(fieldName, type);
    }
}