package recng.recommendations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import recng.cache.CacheBuilder;
import recng.cache.Weigher;
import recng.common.Consumer;
import recng.common.FieldMetadata;
import recng.common.TableMetadata;
import recng.graph.EdgeFilter;
import recng.graph.Graph;
import recng.graph.GraphCursor;
import recng.graph.GraphEdge;
import recng.graph.NodeID;
import recng.graph.Traverser;
import recng.graph.TraverserBuilder;
import recng.recommendations.cache.ProductCache;
import recng.recommendations.cache.ProductCacheImpl;
import recng.recommendations.data.DataStore;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.domain.ImmutableProductImpl;
import recng.recommendations.domain.Product;
import recng.recommendations.domain.ProductImpl;
import recng.recommendations.filter.ProductFilter;
import recng.recommendations.graph.ProductID;

/**
 * Implementation of {@link RecommendationModel} backed by a {@link Graph} of
 * product relations and a {@link DataStore} with product data.
 *
 * @author jon
 *
 * @param <T>
 *            The generic type of the internally stored product IDs.
 */
public class RecommendationModelImpl<T> implements RecommendationModel {

    // The product graph, i.e. the relations between products
    private final Graph<T> productGraph;
    // The backend (db) storage of product data
    private final DataStore productData;
    // Cached product data
    private final ProductCache<T> productCache;
    // Converts a string representation of a product id to an internal
    // representation of the id.
    private final IDFactory<T> idFactory;

    // Statistics counters
    private final AtomicInteger getRelatedcalls = new AtomicInteger();
    private final AtomicInteger traversed = new AtomicInteger();
    private final AtomicInteger filtered = new AtomicInteger();
    private final AtomicInteger cacheHits = new AtomicInteger();
    private final AtomicInteger cacheMisses = new AtomicInteger();

    /**
     *
     * @param productGraph
     *            A graph describing the relations between products.
     * @param productData
     *            An interface to product data backend.
     * @param keyParser
     *            Used to parse a String representation of a product id to
     *            something else.
     */
    public RecommendationModelImpl(Graph<T> productGraph,
                                   DataStore productData,
                                   IDFactory<T> keyParser) {
        this.productGraph = productGraph;
        this.productData = productData;
        this.productCache = setupCache(productGraph);
        this.idFactory = keyParser;

        productGraph.getAllNodes(new Consumer<NodeID<T>, Void>() {

            @Override
            public Void consume(NodeID<T> node) {
                fetchAndCacheProduct(node.getID());
                return null;
            }
        });
    }

    @Override
    public List<ImmutableProduct>
        getRelatedProducts(String sourceProduct,
                           ProductQuery query) {
        getRelatedcalls.incrementAndGet();
        List<ImmutableProduct> res = new ArrayList<ImmutableProduct>();
        NodeID<T> pid = getProductId(sourceProduct);
        if (pid == null)
            return null;
        Traverser<T> traverser = setupTraverser(pid, query);
        if (traverser == null)
            return res;
        GraphCursor<T> cursor = traverser.traverse();
        try {
            while (cursor.hasNext()) {
                GraphEdge<T> edge = cursor.next();
                NodeID<T> related = edge.getEndNode();
                res.add(getProductProperties(related.getID()));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    @Override
    public ImmutableProduct
        getProduct(String productId) {
        T key = idFactory.fromString(productId);
        return getProductProperties(key);
    }

    private NodeID<T> getProductId(String id) {
        T key = idFactory.fromString(id);
        return new ProductID<T>(key);
    }

    private ProductCache<T> setupCache(Graph<T> productGraph) {
        CacheBuilder<T, Product> builder = new CacheBuilder<T, Product>();
        builder.weigher(new Weigher<T, Product>() {
            @Override
            public int weigh(int overhead, T key, Product value) {
                int weight = value != null ? value.getWeight() : 0;
                return overhead +
                    40 + // estimated (maximum) key size, bytes
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
        TraverserBuilder<T> builder =
            productGraph.prepareTraversal(source,
                                          query.getRecommendationType());
        if (builder != null)
            return builder
            .maxReturnedEdges(query.getLimit())
            .maxTraversedEdges(query.getMaxCursorSize())
            .maxDepth(query.getMaxRelationDistance())
            .edgeFilter(new OnlyValidFilter(query.getFilter()))
            .build();
        return null;
    }

    private ImmutableProduct getProductProperties(T productId) {
        if (!productCache.contains(productId))
            return fetchAndCacheProduct(productId);
        cacheHits.incrementAndGet();
        Product cached = productCache.getProduct(productId);
        if (cached == null)
            return null;
        return new ImmutableProductImpl(idFactory.toString(productId), cached);
    }

    private ImmutableProduct fetchAndCacheProduct(T productId) {
        cacheMisses.incrementAndGet();
        Map<String, Object> data =
            productData.getData(idFactory.toString(productId));
        if (data == null) {
            productCache.cacheProduct(productId, null);
            return null;
        }
        Boolean isValidProperty = (Boolean) data.get(Product.IS_VALID_PROPERTY);
        boolean isValid =
            isValidProperty == null || isValidProperty.booleanValue();
        TableMetadata fields = productData.getMetadata();
        String id = idFactory.toString(productId);
        Product product = new ProductImpl(id, isValid, fields);
        for (Map.Entry<String, Object> property : data.entrySet()) {
            String key = property.getKey();
            Object value = property.getValue();
            setProductProperty(product, fields.getFieldMetadata(key), value);
        }
        productCache.cacheProduct(productId, product);
        return new ImmutableProductImpl(id, product);
    }

    private void setProductProperty(Product product,
                                    FieldMetadata<?> field,
                                    Object value) {
        if (field.isRepeated()) {
            setRepeatedProductProperty(product, field, value);
            return;
        }
        product.setProperty(field.getFieldName(), value);
    }

    private void setRepeatedProductProperty(Product product,
                                            FieldMetadata<?> field,
                                            Object value) {
        FieldMetadata.Type type = field.getType();
        String key = field.getFieldName();
        switch (type) {
        case BYTE:
            List<Byte> bytes = implicitCast(value);
            product.setRepeatedProperties(key, bytes);
            break;
        case SHORT:
            List<Short> shorts = implicitCast(value);
            product.setRepeatedProperties(key, shorts);
            break;
        case INTEGER:
            List<Integer> integers = implicitCast(value);
            product.setRepeatedProperties(key, integers);
            break;
        case LONG:
            List<Long> longs = implicitCast(value);
            product.setRepeatedProperties(key, longs);
            break;
        case FLOAT:
            List<Float> floats = implicitCast(value);
            product.setRepeatedProperties(key, floats);
            break;
        case DOUBLE:
            List<Double> doubles = implicitCast(value);
            product.setRepeatedProperties(key, doubles);
            break;
        case BOOLEAN:
            List<Boolean> booleans = implicitCast(value);
            product.setRepeatedProperties(key, booleans);
            break;
        case STRING:
            List<String> strings = implicitCast(value);
            product.setRepeatedProperties(key, strings);
            break;
        case DATE:
            List<Date> dates = implicitCast(value);
            product.setRepeatedProperties(key, dates);
            break;
        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    private <L> L implicitCast(Object value) {
        return (L)value;
    }

    private class OnlyValidFilter implements EdgeFilter<T> {

        private final ProductFilter pFilter;

        public OnlyValidFilter(ProductFilter pFilter) {
            this.pFilter = pFilter;
        }

        public boolean accepts(NodeID<T> start, NodeID<T> end) {
            traversed.incrementAndGet();
            ImmutableProduct product =
                getProductProperties(end.getID());
            boolean accepts = product != null &&
                product.isValid() && pFilter.accepts(product);
            if (!accepts)
                filtered.incrementAndGet();

            return accepts;
        }
    }

    @Override
    public String getStatusString() {
        return String.format("Graph size:%s,\n" +
            "Cache size:%s,\n" +
            "No. calls:%s,\n" +
                                 "Traversed edges:%s\n" +
                                 "Filtered edges:%s (%s percent)\n" +
                                 "Cache hits/misses/percent: %s/%s/%s",
                             productGraph.nodeCount(), productCache.size(),
                             getRelatedcalls.get(), traversed.get(),
                             filtered.get(),
                             100.0 * filtered.get() / traversed.get(),
                             cacheHits.get(),
                             cacheMisses.get(),
                             100.0 * cacheHits.get()
                                 / (cacheHits.get() + cacheMisses.get()));
    }
}
