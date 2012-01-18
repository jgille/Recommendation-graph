package recng.recommendations.jmeter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import recng.common.TableMetadata;
import recng.db.KVStore;
import recng.db.mongodb.MongoKVStore;
import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphImporter;
import recng.graph.GraphImporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.ImmutableGraphImpl;
import recng.graph.NodeID;
import recng.graph.NodeType;
import recng.index.ID;
import recng.index.StringIDs;
import recng.recommendations.RecommendationManager;
import recng.recommendations.RecommendationManagerImpl;
import recng.recommendations.RecommendationModel;
import recng.recommendations.RecommendationModelImpl;
import recng.recommendations.StringIDFactory;
import recng.recommendations.data.DataStore;
import recng.recommendations.data.DataStoreImpl;
import recng.recommendations.graph.ProductID;
import recng.recommendations.graph.RecommendationGraphMetadata;
import tests.misc.TestMongoDataUploader;

/**
 * A simple {@link AbstractJavaSamplerClient} used to load test a
 * {@link RecommendationManager}.
 *
 * @author jon
 *
 */
public class RecommendationSamplerImpl extends AbstractRecommendationSampler {

    private static TestRunner runner;

    // I can not get JMeter to store default variables. For now I keep these.
    private static final String DEFAULT_CUSTOMER =
        "suomalainen"; // TODO: Eh...
    private static final String DEFAULT_DIR =
        String.format("/home/jon/%s", DEFAULT_CUSTOMER); // TODO: Eh...

    private static final Object LOCK = new Object();

    @Override
    public void setupTest(JavaSamplerContext ctx) {
        synchronized (LOCK) {
            Map<String, String> config =
                new HashMap<String, String>();
            config.put("graph", ctx.getParameter("graph",
                                                 String.format("%s/graph.csv",
                                                               DEFAULT_DIR)));
            config.put("metadata",
                       ctx.getParameter("metadata",
                                        String.format("%s/productformat",
                                                      DEFAULT_DIR)));
            config.put("products",
                       ctx.getParameter("products",
                                        String.format("%s/products",
                                                      DEFAULT_DIR)));
            config.put("db",
                       ctx.getParameter("customer", DEFAULT_CUSTOMER));
            System.out.println("Config = " + config);
            if (runner == null)
                runner = new TestRunner(config);
        }
    }

    @Override
    protected RecommendationManager getManager() {
        return runner.getManager();
    }
    @Override
    protected String getNextProduct() {
        return runner.getNextProduct();
    }

    private static class TestRunner {
        private final RecommendationManager manager;
        private final List<String> products;
        private final AtomicInteger current = new AtomicInteger();

        private TestRunner(Map<String, String> config) {
            try {
                System.out.println("Setting up test runner");
                this.manager =
                    new RecommendationManagerImpl(setupModel(config));
                System.out.println("Getting clicks");
                this.products =
                    getInputProducts(config.get("products"), 100000);
                System.out.println("Done");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private RecommendationModel setupModel(Map<String, String> config)
            throws IOException {
            return new RecommendationModelImpl<ID<String>>(
                                                           setupGraph(config),
                                                           setupDataStore(config),
                                                           new StringIDFactory());
        }

        private Graph<ID<String>> setupGraph(Map<String, String> config)
        throws IOException {
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
            String file = config.get("graph");
        Graph<ID<String>> graph = importer.importGraph(file);
        long t1 = System.currentTimeMillis();
        System.out.println("Done with graph import. Imported "
            + graph.nodeCount()
            + " nodes and " + graph.edgeCount() + " edges in " +
            (t1 - t0) + " ms.");

        return graph;
    }

        private DataStore setupDataStore(Map<String, String> config)
            throws IOException {
            TableMetadata metadata =
                readTableMetadata(config.get("metadata"));
            final KVStore<String, Map<String, Object>> mongoStore =
                new MongoKVStore();
            Map<String, String> mongoProperties = new HashMap<String, String>();
            mongoProperties.put("db", config.get("db"));
            mongoProperties.put("collection", "productdata");
            mongoStore.init(mongoProperties);
            DataStore productData = new DataStoreImpl(mongoStore, metadata);
            return productData;
        }

        private TableMetadata readTableMetadata(String file) throws IOException {
            return TestMongoDataUploader.parseTableMetadata(file);
        }

        private String getNextProduct() {
            int index = current.getAndIncrement();
            if (index >= products.size()) {
                index = 0;
                current.set(0);
            }
            return products.get(index);
        }

        private static List<String> getInputProducts(String clickFile, int limit)
            throws IOException {
            List<String> products = new ArrayList<String>();
            BufferedReader reader =
                new BufferedReader(new FileReader(clickFile));
            String prev = null;
            String line;
            while ((line = reader.readLine()) != null && products.size() < limit) {
                String[] columns = line.split(";");
                if (columns.length != 2)
                    continue;
                String product = columns[1].replaceAll("\"", "");
                if (!product.equals(prev))
                    products.add(product);
                prev = product;
            }
            return products;
        }

        private RecommendationManager getManager() {
            return manager;
        }
    }
}
