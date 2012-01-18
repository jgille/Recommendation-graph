package tests.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import recng.cache.Cache;
import recng.common.BinPropertyContainer;
import recng.common.Consumer;
import recng.common.PropertyContainer;
import recng.common.TableMetadata;
import recng.db.KVStore;
import recng.db.mongodb.MongoKVStore;
import recng.graph.*;
import recng.index.*;
import recng.recommendations.IDFactory;
import recng.recommendations.RecommendationManager;
import recng.recommendations.RecommendationManagerImpl;
import recng.recommendations.RecommendationModel;
import recng.recommendations.RecommendationModelImpl;
import recng.recommendations.StringIDFactory;
import recng.recommendations.data.DataStore;
import recng.recommendations.data.DataStoreImpl;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.graph.ProductID;
import recng.recommendations.graph.RecommendationGraphMetadata;

public class TestGraphImporterImpl {

    public static void main(String[] args) throws UnknownHostException,
        IOException {
        String file = args[0];
        GraphMetadata metadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            new ImmutableGraphImpl.Builder<ID<String>>(metadata);
        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, metadata) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id,
                                                        NodeType nodeType) {
                    return new ProductID<ID<String>>(StringIDs.parseKey(id));
                }
            };
        long t0 = System.currentTimeMillis();
        Graph<ID<String>> graph = importer.importGraph(file);
        long t1 = System.currentTimeMillis();
        System.out.println("Done with graph import. Imported "
            + graph.nodeCount()
            + " nodes and " + graph.edgeCount() + " edges in " +
            (t1 - t0) + " ms.");
        importer = null;
        builder = null;

        final TableMetadata tableMetadata =
            TestMongoDataUploader.parseTableMetadata(args[2]);

        /*
        long t2 = System.currentTimeMillis();
        CacheBuilder<ID<String>, PropertyContainer> cacheBuilder =
            new CacheBuilder<ID<String>, PropertyContainer>();
        final Cache<ID<String>, PropertyContainer> cache =
            cacheBuilder.maxSize(100000).build();

        warmCache(graph, cache, args[1], tableMetadata);
        long t3 = System.currentTimeMillis();
        System.out.println("Fetched product data for " + cache.size() +
            " products in " + (t3 - t2) + " ms.");
            */

        final IDFactory<ID<String>> keyParser = new StringIDFactory();

        /*
        DataStore productData = new DataStore() {

            @Override
            public TableMetadata getMetadata() {
                return tableMetadata;
            }

            @Override
            public Map<String, Object> getData(String id) {
                PropertyContainer props = cache.get(keyParser.fromString(id));
                if (props == null)
                    return null;
                Map<String, Object> m = new HashMap<String, Object>();
                for (String key : props.getKeys())
                    m.put(key, props.get(key));
                return m;
            }
        };
        */
        final KVStore<String, Map<String, Object>> mongoStore =
            new MongoKVStore();
        Map<String, String> mongoProperties = new HashMap<String, String>();
        mongoProperties.put("db", args[1]);
        mongoProperties.put("collection", "productdata");
        mongoStore.init(mongoProperties);
        DataStore productData = new DataStoreImpl(mongoStore, tableMetadata);
        RecommendationModel model =
            new RecommendationModelImpl<ID<String>>(graph, productData,
                                                    keyParser);
        RecommendationManager manager = new RecommendationManagerImpl(model);

        /*
        // Warm the "cache"
        testRecommendations(args[3], manager, tableMetadata);
        System.out.println("Starting recommendation test");
        long t4 = System.currentTimeMillis();
        int recs =
            testRecommendations(args[3], manager, tableMetadata);
        long t5 = System.currentTimeMillis();
        System.out.println("Made " + recs +
            " recommendations in " + (t5 - t4) + " ms.");
        System.out.println(manager.toString());
        */

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private static List<String> getClicks(String clickFile, int limit)
        throws IOException {
        List<String> clicks = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(clickFile));
        String prev = null;
        String line;
        while ((line = reader.readLine()) != null && clicks.size() < limit) {
            String[] columns = line.split(";");
            if (columns.length != 2)
                continue;
            String product = columns[1].replaceAll("\"", "");
            if (!product.equals(prev))
                clicks.add(product);
            prev = product;
        }
        Collections.shuffle(clicks);
        return clicks;
    }

    private static
        int
        testRecommendations(String clickFile,
                            RecommendationManager manager,
                            final TableMetadata metadata)
        throws IOException {
        final List<String> products = getClicks(clickFile, 100000);
        final Set<String> fields = new HashSet<String>(metadata.getFields());
        int count = 0;
        for (String product : products) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("ProductID", product);
            params.put("TemplateName", "test");
            params.put("Fields", join(fields));
            List<ImmutableProduct> recommendation =
                manager.getRelatedProducts(params);
            count += recommendation.size();
        }
        return count;
    }

    private static String join(Collection<String> col) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String val : col) {
            if (!first)
                sb.append(",");
            first = false;
            sb.append(val);
        }
        return sb.toString();
    }

    private static void warmCache(Graph<ID<String>> graph,
                  final Cache<ID<String>, PropertyContainer> cache,
                  String dbName, final TableMetadata metadata)
            throws UnknownHostException, IOException {
        Mongo mongo = new Mongo();
        DB db = mongo.getDB(dbName);
        final DBCollection col = db.getCollection("productdata");
        graph.getAllNodes(new Consumer<NodeID<ID<String>>, Void>() {

            @Override
            public Void consume(NodeID<ID<String>> node) {
                String id = node.getID().getID();
                DBObject dbo = col.findOne(new BasicDBObject("_id", id));
                if (dbo == null) {
                    return null;
                }
                // PropertyContainer props = new PropertyContainerImpl();
                PropertyContainer props =
                    new BinPropertyContainer(metadata, false);
                for (String key : dbo.keySet()) {
                    if ("_id".equals(key))
                        continue;
                    props.set(key, dbo.get(key));
                }
                cache.cache(node.getID(), props);
                return null;
            }
        });
        mongo.close();
    }
}