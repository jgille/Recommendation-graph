package recng.recommendations.jmeter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

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
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.graph.ProductID;
import recng.recommendations.graph.RecommendationGraphMetadata;
import tests.misc.TestMongoDataUploader;

public class RecommendationSampler extends AbstractJavaSamplerClient {

    private static final TestRunner RUNNER = new TestRunner();
    private static final AtomicBoolean DONE = new AtomicBoolean(false);

    @Override
    public void setupTest(JavaSamplerContext arg0) {
        DONE.getAndSet(false);
    }

    @Override
    public void teardownTest(JavaSamplerContext arg0) {
        if (!DONE.getAndSet(true))
            System.out.println(RUNNER.manager);
    }

    @Override
    public SampleResult runTest(JavaSamplerContext ctx) {
        SampleResult sr = new SampleResult();
        sr.sampleStart();
        List<ImmutableProduct> rec = RUNNER
            .getRecommendation(new HashMap<String, String>());
        // System.out.println(rec);
        sr.setResponseMessage(rec + "");
        sr.sampleEnd();
        sr.setSuccessful(true);
        return sr;
    }

    private String getResponse(String product,
                               List<ImmutableProduct> recommendation) {
        return recommendation == null ? product + " -> Null" :
            product + " ->\n" + recommendation.toString();
    }

    private static class TestRunner {
        private final RecommendationManager manager;
        private final List<String> products;
        private final AtomicInteger current = new AtomicInteger();

        private static final String CUSTOMER = "suomalainen";

        private TestRunner() {
            try {
                System.out.println("Setting up test runner");
                Map<String, String> config = new HashMap<String, String>();
                config.put("graph_file",
                           String.format("/home/jon/%s/graph.csv", CUSTOMER));
                config.put("metadata_file",
                           String
                               .format("/home/jon/%s/productformat", CUSTOMER));
                config.put("db", CUSTOMER);

                System.out.println("Setting up test manager");
                this.manager =
                    new RecommendationManagerImpl(setupModel(config));
                System.out.println("Getting clicks");
                this.products =
                    getClicks(String.format("/home/jon/%s/shuf_click",
                                            CUSTOMER),
                              100000);
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
        String file = config.get("graph_file");
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
                readTableMetadata(config.get("metadata_file"));
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

        private List<ImmutableProduct>
            getRecommendation(Map<String, String> params) {
            if (!params.containsKey("ProductID"))
                params.put("ProductID", getNextProduct());
            return manager.getRelatedProducts(params);
        }

        private String getNextProduct() {
            int index = current.getAndIncrement();
            if (index >= products.size()) {
                index = 0;
                current.set(0);
            }
            return products.get(index);
        }

        private static List<String> getClicks(String clickFile, int limit)
            throws IOException {
            List<String> clicks = new ArrayList<String>();
            BufferedReader reader =
                new BufferedReader(new FileReader(clickFile));
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
            // Collections.shuffle(clicks);
            return clicks;
        }

    }
}
