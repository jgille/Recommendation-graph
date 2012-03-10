package recng.recommendations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import recng.cache.CacheBuilder;
import recng.cache.Weigher;
import recng.common.FieldMetadata;
import recng.common.TableMetadata;
import recng.graph.Graph;
import recng.graph.GraphCursor;
import recng.graph.GraphEdge;
import recng.graph.NodeID;
import recng.graph.NodeIDProcedure;
import recng.graph.Traverser;
import recng.profiling.AbstractProfiler;
import recng.profiling.CappedInMemoryProfiler;
import recng.profiling.ProfilerEntry;
import recng.profiling.ProfilerEntryImpl;
import recng.profiling.ProfilerSettings;
import recng.recommendations.cache.ProductCache;
import recng.recommendations.cache.ProductCacheImpl;
import recng.recommendations.data.ProductRepository;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.domain.ImmutableProductImpl;
import recng.recommendations.domain.Product;
import recng.recommendations.domain.ProductImpl;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.graph.OnlyValidFilter;

/**
 * Implementation of {@link RecommendationModel} backed by a {@link Graph} of
 * product relations and a {@link ProductRepository} with product data.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the internally stored product IDs.
 */
public class RecommendationModelImpl<T> implements RecommendationModel,
    ImmutableProductRepository<T> {

    // The product graph, i.e. the relations between products
    private final Graph<T> productGraph;
    // The backend (db) storage of product data
    private final ProductRepository productRepository;
    // Cached product data
    private final ProductCache<T> productCache;
    // Converts a string representation of a product id to an internal
    // representation of the id.
    private final IDParser<T> idParser;
    private final AbstractProfiler profiler;

    /**
     *
     * @param productGraph
     *            A graph describing the relations between products.
     * @param productRepository
     *            An interface to product some data storage.
     * @param idParser
     *            Used to parse a String representation of a product id to
     *            something else.
     */
    public RecommendationModelImpl(Graph<T> productGraph,
                                   ProductRepository productRepository,
                                   IDParser<T> idParser) {
        this.productGraph = productGraph;
        this.productRepository = productRepository;
        // TODO: Get cache instance as constructor parameter instead
        this.productCache = setupCache(productGraph);
        this.idParser = idParser;
        // Warm up the cache
        int nNodes = productGraph.nodeCount();
        System.out.println(String.format("Warming the cache for %s products",
                                         nNodes));
        productGraph.forEachNode(new NodeIDProcedure<T>() {
            @Override
            public boolean apply(NodeID<T> node) {
                fetchAndCacheProduct(node.getID());
                return true;
            }
        });
        System.out.println("Done.");
        this.profiler = new CappedInMemoryProfiler(1000);
    }

    private ProfilerEntry startProfiling(String description) {
        ProfilerEntry entry = new ProfilerEntryImpl(description) {
            private static final long serialVersionUID = 1L;

            @Override
            public void finish() {
                super.finish();
                profiler.logProfilerEntry(this);
            }
        };
        entry.start();
        return entry;
    }

    @Override
    public List<ImmutableProduct>
        getRelatedProducts(String sourceProduct,
                           final ProductQuery query) {
        NodeID<T> pid = getProductId(sourceProduct);
        if (pid == null)
            return null;
        if (query.getMaxRelationDistance() == 1)
            return getProductNeighbors(pid, query);
        ProfilerEntry profilerEntry = startProfiling("getRelatedProducts");
        profilerEntry.setProperty("source", sourceProduct);
        profilerEntry.setProperty("query", query);
        // Setup a traverser and use it to iterate the related products (the
        // node neighbors)
        Traverser<T> traverser = setupTraverser(pid, query);
        return getTraversedProducts(traverser, profilerEntry);
    }

    /**
     * Gets all immediate neighbors matching the query.
     */
    private List<ImmutableProduct> getProductNeighbors(NodeID<T> pid, ProductQuery query) {
        ProfilerEntry profilerEntry = startProfiling("getProductNeighbors");
        profilerEntry.setProperty("source", pid.getID());
        profilerEntry.setProperty("query", query);

        GetProcuctNeighborsProcedure<T> proc =
            new GetProcuctNeighborsProcedure<T>(this, pid, query, profilerEntry);
        productGraph.forEachNeighbor(pid, query.getRecommendationType(), proc);
        return proc.getProducts();
    }

    /**
     * @see Graph#getMultiTraverser(List, recng.graph.EdgeType)
     */
    @Override
    public List<ImmutableProduct>
        getRelatedProducts(List<String> sourceProducts, ProductQuery query) {
        ProfilerEntry profilerEntry = startProfiling("getRelatedProducts");
        profilerEntry.setProperty("sources", sourceProducts);
        profilerEntry.setProperty("query", query);
        List<NodeID<T>> sourceNodes = new ArrayList<NodeID<T>>();
        for (String sourceProduct : sourceProducts) {
            NodeID<T> pid = getProductId(sourceProduct);
            if (pid == null)
                continue;
            sourceNodes.add(pid);
        }
        if (sourceNodes.isEmpty())
            return Collections.emptyList();
        // Setup a traverser and use it to iterate the related products (the
        // node neighbors)
        Traverser<T> traverser = setupMultiTraverser(sourceNodes, query);
        return getTraversedProducts(traverser, profilerEntry);
    }

    private List<ImmutableProduct> getTraversedProducts(Traverser<T> traverser,
                                                        ProfilerEntry profilerEntry) {
        if (traverser == null)
            return Collections.emptyList();
        List<ImmutableProduct> res = new ArrayList<ImmutableProduct>();
        GraphCursor<T> cursor = traverser.traverse();
        try {
            while (cursor.hasNext()) {
                GraphEdge<T> edge = cursor.next();
                NodeID<T> related = edge.getEndNode();
                // Get product properties for the neighbor node
                res.add(getImmutableProduct(related.getID()));
            }
        } finally {
            cursor.close();
        }
        profilerEntry.setProperty("nTraversed", cursor.getTraversedEdgeCount());
        profilerEntry.setProperty("nReturned", cursor.getReturnedEdgeCount());
        profilerEntry.finish();
        return res;
    }

    @Override
    public ImmutableProduct getProduct(String productId) {
        T id = idParser.parse(productId);
        return getImmutableProduct(id);
    }

    private NodeID<T> getProductId(String productId) {
        T id = idParser.parse(productId);
        return new NodeID<T>(id, RecommendationNodeType.PRODUCT);
    }

    private ProductCache<T> setupCache(Graph<T> productGraph) {
        CacheBuilder<T, Product> builder = new CacheBuilder<T, Product>();
        builder.weigher(new Weigher<T, Product>() {
            @Override
            public int weigh(int overhead, T key, Product value) {
                int weight = value != null ? value.getWeight() : 0;
                return overhead +
                    40 + // estimated (maximum) key size in bytes
                    weight;
            }
        });
        // TODO: Must be configurable
        builder.maxWeight(Runtime.getRuntime().maxMemory() / 4);
        // Caching data for all nodes in the graph should suffice?
        builder.maxSize(productGraph.nodeCount());
        final ProductCache<T> cache = new ProductCacheImpl<T>(builder.build());
        return cache;
    }

    private Traverser<T> setupTraverser(NodeID<T> source,
                                        ProductQuery query) {
        Traverser<T> traverser =
            productGraph.getTraverser(source, query.getRecommendationType());
        return configureTraverser(traverser, query);
    }

    private Traverser<T> setupMultiTraverser(List<NodeID<T>> sources,
                                             ProductQuery query) {
        Traverser<T> traverser =
            productGraph.getMultiTraverser(sources, query.getRecommendationType());
        return configureTraverser(traverser, query);
    }

    private Traverser<T> configureTraverser(Traverser<T> traverser, ProductQuery query) {
        if (traverser == null)
            return null;
        return traverser
            .setMaxReturnedEdges(query.getLimit())
            .setMaxTraversedEdges(query.getMaxCursorSize())
            .setMaxDepth(query.getMaxRelationDistance())
            .setReturnableFilter(new OnlyValidFilter<T>(query.getFilter(), this));
    }

    @Override
    public ImmutableProduct getImmutableProduct(T productId) {
        Product cached = productCache.getProduct(productId);
        if (cached != null)
            return new ImmutableProductImpl(cached);
        if (!productCache.contains(productId))
            return fetchAndCacheProduct(productId);
        return null;
    }

    /**
     * Fetch product data from the data store and cache it.
     */
    private ImmutableProduct fetchAndCacheProduct(T productId) {
        Map<String, Object> data =
            productRepository.getProductData(idParser.serialize(productId));
        if (data == null) {
            productCache.cacheProduct(productId, null);
            return null;
        }
        Boolean isValidProperty = (Boolean) data.get(Product.IS_VALID_PROPERTY);
        boolean isValid =
            isValidProperty == null || isValidProperty.booleanValue();
        TableMetadata fields = productRepository.getMetadata();
        Product product = new ProductImpl(isValid, fields);
        for (Map.Entry<String, Object> property : data.entrySet()) {
            String key = property.getKey();
            Object value = property.getValue();
            setProductProperty(product, fields.getFieldMetadata(key), value);
        }
        productCache.cacheProduct(productId, product);
        return new ImmutableProductImpl(product);
    }

    private void setProductProperty(Product product,
                                    FieldMetadata field,
                                    Object value) {
        if (field.isRepeated()) {
            setRepeatedProductProperty(product, field, value);
            return;
        }
        product.setProperty(field.getFieldName(), value);
    }

    private void setRepeatedProductProperty(Product product,
                                            FieldMetadata field,
                                            Object value) {
        String key = field.getFieldName();
        List<Object> valueList = implicitCast(value);
        product.setRepeatedProperties(key, valueList);
    }

    @SuppressWarnings("unchecked")
    private <L> L implicitCast(Object value) {
        return (L) value;
    }

    @Override
    public void setProfilerSettings(ProfilerSettings settings) {
        profiler.setProfilerSettings(settings);
    }

    @Override
    public ProfilerSettings getProfilerSettings() {
        return profiler.getProfilerSettings();
    }

    @Override
    public Iterator<ProfilerEntry> getProfilerEntries() {
        return profiler.getProfilerEntries();
    }

    @Override
    public String toString() {
        return "RecommendationModelImpl [productGraph=" + productGraph + ", productRepository="
            + productRepository + ", productCache=" + productCache + ", idParser=" + idParser
            + ", profiler=" + profiler + "]";
    }
}
