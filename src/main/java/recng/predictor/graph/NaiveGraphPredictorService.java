package recng.predictor.graph;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mahout.math.function.IntIntProcedure;
import org.apache.mahout.math.function.IntProcedure;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.apache.mahout.math.set.AbstractIntSet;

import recng.graph.EdgeType;
import recng.graph.Graph;
import recng.graph.GraphBuilder;
import recng.graph.GraphExporter;
import recng.graph.GraphExporterImpl;
import recng.graph.GraphMetadata;
import recng.graph.GraphMetadataImpl;
import recng.graph.ImmutableGraphImpl;
import recng.graph.NodeID;
import recng.graph.NodeType;
import recng.index.ID;
import recng.index.StringIDs;
import recng.predictor.PredictorBaseData;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.RecommendationEdgeType;

/**
 * Naive prediction service producing a graph.
 * 
 * @author jon
 * 
 */
public class NaiveGraphPredictorService extends
    AbstractGraphPredictorService<ID<String>> {

    private final Object lock = new Object();

    private GraphMetadata getGraphMetadata() {
        Set<EdgeType> edgeTypes = new HashSet<EdgeType>();
        edgeTypes.addAll(EnumSet.allOf(RecommendationEdgeType.class));
        Set<NodeType> nodeTypes =
            new HashSet<NodeType>(EnumSet.allOf(RecommendationNodeType.class));
        return new GraphMetadataImpl(nodeTypes, edgeTypes);
    }

    @Override
    protected Graph<ID<String>> createPredictions(PredictorBaseData baseData) {
        GraphMetadata metadata = getGraphMetadata();
        GraphBuilder<ID<String>> builder =
            ImmutableGraphImpl.Builder.create(metadata);
        IntArrayList allProducts = baseData.getAllProducts();
        System.out.println("Setting up recommendation graph...");
        addEdges(builder, allProducts, baseData,
                 RecommendationEdgeType.PEOPLE_WHO_BOUGHT);
        addEdges(builder, allProducts, baseData,
                 RecommendationEdgeType.PEOPLE_WHO_VIEWED);
        Graph<ID<String>> graph = builder.build();
        System.out.println("Done!");
        return graph;
    }

    private float getWeight(int totRelated, int related) {
        return 1f * related / totRelated;
    }

    private NodeID<ID<String>> getProductID(String productID) {
        return new NodeID<ID<String>>(StringIDs.parseID(productID),
                                      RecommendationNodeType.PRODUCT);
    }

    /**
     * Adds product->product edges of the provided
     * {@link RecommendationEdgeType}.
     */
    private void addEdges(final GraphBuilder<ID<String>> builder,
                          final IntArrayList allProducts,
                          final PredictorBaseData baseData,
                          final RecommendationEdgeType recType) {
        System.out.println("Creating " + recType + " relations...");
        // Use a thread pool for increased performance
        final ExecutorService service =
            Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors());
        // Counter used for printouts
        final AtomicInteger productCounter = new AtomicInteger();
        final AtomicInteger edgeCounter = new AtomicInteger();
        allProducts.forEach(new IntProcedure() {

            @Override
            public boolean apply(int product) {
                // Add edges from this product
                int edgeCount =
                    addProductEdges(builder, baseData, recType, product);
                int totalEdgeCount = edgeCounter.addAndGet(edgeCount);
                int totalProductCount = productCounter.incrementAndGet();
                if (totalProductCount % 5000 == 0) {
                    System.out.println(String
                        .format("Done for %s of %s products",
                                totalProductCount, allProducts.size()));
                    System.out.println(String.format("Added %s %s edges",
                                                     totalEdgeCount,
                                                     recType));
                }
                return true;
            }
        });
        service.shutdown();
        try {
            service.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean addBaseEdges() {
        return false;
    }

    private int addProductEdges(final GraphBuilder<ID<String>> builder,
                                final PredictorBaseData baseData,
                                final RecommendationEdgeType recType,
                                final int product) {
        // product index -> nof occurrences
        final OpenIntIntHashMap productCounts = new OpenIntIntHashMap();
        // users or sessions
        AbstractIntSet connectors =
            getConnectors(baseData, product, recType);
        if (connectors == null)
            return 0;
        // Iterate all users/sessions that have bought viewed this product.
        // For these, iterate the products they have also bougth/viewed and
        // group them by index.
        connectors.forEachKey(new IntProcedure() {

            @Override
            public boolean apply(int connector) {
                // user/session index
                if (addBaseEdges())
                    addBaseEdge(builder, baseData, recType, connector, product);
                // Viewed or bought products
                AbstractIntSet products =
                    getProducts(baseData, connector, recType);
                if (products == null)
                    return true;
                products.forEachKey(new IntProcedure() {

                    @Override
                    public boolean apply(int otherProduct) {
                        if (otherProduct == product)
                            return true;
                        // increment counter
                        productCounts.adjustOrPutValue(otherProduct, 1, 1);
                        return true;
                    }
                });
                return true;
            }
        });
        // Get total related count used to normalize edge weights
        final AtomicInteger related = new AtomicInteger(0);
        IntArrayList counts = productCounts.values();
        counts.forEach(new IntProcedure() {

            @Override
            public boolean apply(int count) {
                if (count > 1)
                    related.addAndGet(count);
                return true;
            }
        });

        final String productID = baseData.getProductID(product);
        final AtomicInteger added = new AtomicInteger(0);

        // Iterate all related products and add weighted graph edges to them
        productCounts.forEachPair(new IntIntProcedure() {

            @Override
            public boolean apply(int otherProduct, int count) {
                if (count < 2)
                    return true;
                String otherProductID = baseData.getProductID(otherProduct);
                // Must be synchronized since we're using a thread pool
                synchronized (lock) {
                    NodeID<ID<String>> sourceProduct = getProductID(productID);
                    NodeID<ID<String>> recommendedProduct =
                        getProductID(otherProductID);
                    int start = builder.addOrGetNode(sourceProduct);
                    int end = builder.addOrGetNode(recommendedProduct);
                    float weight = getWeight(related.get(), count);
                    builder
                        .addEdge(start, end, recType, weight);
                }
                added.incrementAndGet();
                return true;
            }
        });
        return added.get();
    }

    private void addBaseEdge(GraphBuilder<ID<String>> builder,
                             PredictorBaseData baseData,
                             RecommendationEdgeType recType,
                             int connector,
                             int product) {
        String productID = baseData.getProductID(product);
        EdgeType edgeType = null;
        String connectorID = null;
        NodeID<ID<String>> connectorNode = null;

        switch (recType) {
        case PEOPLE_WHO_BOUGHT:
            connectorID = baseData.getUserID(connector);
            edgeType = RecommendationEdgeType.BOUGHT;
            connectorNode =
                new NodeID<ID<String>>(StringIDs.parseID(connectorID),
                                       RecommendationNodeType.USER);
            break;
        case PEOPLE_WHO_VIEWED:
            connectorID = baseData.getSessionID(connector);
            connectorNode =
                new NodeID<ID<String>>(StringIDs.parseID(connectorID),
                                       RecommendationNodeType.SESSION);

            edgeType = RecommendationEdgeType.VIEWED;
            break;
        default:
            throw new IllegalArgumentException("Illegal rec type: " + recType);
        }
        synchronized (lock) {
            int start = builder.addOrGetNode(connectorNode);
            NodeID<ID<String>> productNode = getProductID(productID);
            int end = builder.addOrGetNode(productNode);
            float weight = 0;
            builder
                .addEdge(start, end, edgeType, weight);
        }
    }

    private AbstractIntSet getConnectors(PredictorBaseData baseData,
                                         int product,
                                         RecommendationEdgeType recType) {
        switch (recType) {
        case PEOPLE_WHO_BOUGHT:
            return baseData.getBuyers(product);
        case PEOPLE_WHO_VIEWED:
            return baseData.getViewers(product);
        default:
            throw new IllegalArgumentException("Unknown type: " + recType);
        }
    }

    private AbstractIntSet getProducts(PredictorBaseData baseData,
                                       int connector,
                                       RecommendationEdgeType recType) {
        switch (recType) {
        case PEOPLE_WHO_BOUGHT:
            return baseData.getPurchasedProducts(connector);
        case PEOPLE_WHO_VIEWED:
            return baseData.getViewedProducts(connector);
        default:
            throw new IllegalArgumentException("Unknown type: " + recType);
        }
    }

    public static void main(String[] args) {
        GraphPredictorService<ID<String>> service =
            new NaiveGraphPredictorService();
        Map<String, String> config = new HashMap<String, String>();
        config.put("transaction_data_file", args[0]);
        config.put("click_data_file", args[1]);
        service.init(config);
        System.out.println("Creating recommendations.");
        long t0 = System.currentTimeMillis();
        Graph<ID<String>> graph = service.createPredictions();
        long t1 = System.currentTimeMillis();
        System.out.println(graph);
        System.out.println("Done creating recommendation graph in " +
            (t1 - t0) + " ms.");
        if (args.length > 2) {
            String out = args[2];
            System.out.println(String.format("Exporting graph to: %s", out));
            GraphExporter<ID<String>> exporter =
                new GraphExporterImpl<ID<String>>() {

                    @Override
                    protected String serializeNodeID(ID<String> nodeID) {
                        return nodeID.getID();
                    }
                };
            long t2 = System.currentTimeMillis();
            exporter.exportGraph(graph, out);
            long t3 = System.currentTimeMillis();
            System.out.println(String.format("Exported to %s in %s ms", out,
                                             (t3 - t2)));
        }
    }
}
