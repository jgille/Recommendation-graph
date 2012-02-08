package recng.recommendations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import recng.recommendations.cache.ProductCache;
import recng.recommendations.cache.ProductCacheImpl;
import recng.recommendations.data.ProductRepository;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.domain.ImmutableProductImpl;
import recng.recommendations.domain.Product;
import recng.recommendations.domain.ProductImpl;
import recng.recommendations.domain.RecommendationNodeType;
import recng.recommendations.filter.ProductFilter;

/**
 * Implementation of {@link RecommendationModel} backed by a {@link Graph} of
 * product relations and a {@link ProductRepository} with product data.
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
    private final ProductRepository productRepository;
    // Cached product data
    private final ProductCache<T> productCache;
    // Converts a string representation of a product id to an internal
    // representation of the id.
    private final IDParser<T> idParser;

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
        this.productCache = setupCache(productGraph);
        this.idParser = idParser;
        // Warm up the cache
        int nNodes = productGraph.nodeCount();
        System.out.println(String.format("Warming the cache for %s products",
                                         nNodes));
        productGraph.getAllNodes(new Consumer<NodeID<T>, Void>() {
            @Override
            public Void consume(NodeID<T> node) {
                fetchAndCacheProduct(node.getID());
                return null;
            }
        });
        System.out.println("Done.");
    }

    @Override
    public List<ImmutableProduct>
        getRelatedProducts(String sourceProduct,
                           ProductQuery query) {
        List<ImmutableProduct> res = new ArrayList<ImmutableProduct>();
        NodeID<T> pid = getProductId(sourceProduct);
        if (pid == null)
            return null;
        // Setup a traverser and use it to iterate the related products (the
        // node neighbors)
        Traverser<T> traverser = setupTraverser(pid, query);
        if (traverser == null)
            return res;
        GraphCursor<T> cursor = traverser.traverse();
        try {
            while (cursor.hasNext()) {
                GraphEdge<T> edge = cursor.next();
                NodeID<T> related = edge.getEndNode();
                // Get product properties for the neighbor node
                res.add(getProductProperties(related.getID()));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    @Override
    public ImmutableProduct getProduct(String productId) {
        T id = idParser.parse(productId);
        return getProductProperties(id);
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
        if (traverser != null)
            return traverser
                .setMaxReturnedEdges(query.getLimit())
                .setMaxTraversedEdges(query.getMaxCursorSize())
                .setMaxDepth(query.getMaxRelationDistance())
                .setReturnableFilter(new OnlyValidFilter(query.getFilter()));

        return null;
    }

    private ImmutableProduct getProductProperties(T productId) {
        if (!productCache.contains(productId))
            return fetchAndCacheProduct(productId);
        Product cached = productCache.getProduct(productId);
        if (cached == null)
            return null;
        return new ImmutableProductImpl(idParser.serialize(productId), cached);
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
        String id = idParser.serialize(productId);
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
        return (L)value;
    }

    private class OnlyValidFilter implements EdgeFilter<T> {

        private final ProductFilter pFilter;

        public OnlyValidFilter(ProductFilter pFilter) {
            this.pFilter = pFilter;
        }

        public boolean accepts(NodeID<T> start, NodeID<T> end) {
            ImmutableProduct product =
                getProductProperties(end.getID());
            boolean accepts = product != null &&
                product.isValid() && pFilter.accepts(product);
            return accepts;
        }
    }

    @Override
    public String getStatusString() {
        return String.format("Graph size (nodes|edges): %s|%s,\nCache size:%s",
                             productGraph.nodeCount(),
                             productGraph.edgeCount(),
                             productCache.size());
    }
}
