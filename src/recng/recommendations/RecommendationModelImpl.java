package recng.recommendations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import recng.cache.Cache;
import recng.cache.CacheBuilder;
import recng.cache.Weigher;
import recng.common.FieldMetadata;
import recng.common.TableMetadata;
import recng.graph.EdgeFilter;
import recng.graph.Graph;
import recng.graph.GraphCursor;
import recng.graph.GraphEdge;
import recng.graph.NodeID;
import recng.graph.Traverser;
import recng.recommendations.filter.ProductFilter;

/**
 * Implementation of {@link RecommendationModel} backed by a {@link Graph} of
 * product relations and a {@link ProductDataStore} with product data.
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
    private final ProductDataStore productData;
    // Cached product data
    private final ProductCache<T> productCache;
    // Short lived cache used to avoid making duplicate db requests for the same
    // product in close succession
    private final Cache<T, Map<String, Object>> shortTermCache =
        new CacheBuilder<T, Map<String, Object>>().maxSize(100).build();
    // Converts a string representation of a product id to an internal
    // representation of the id.
    private final IDFactory<T> idFactory;

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
                                   ProductDataStore productData,
                                   IDFactory<T> keyParser) {
        this.productGraph = productGraph;
        this.productData = productData;
        this.productCache = setupCache(productGraph);
        this.idFactory = keyParser;
    }

    public List<ImmutableProduct>
        getRelatedProducts(String sourceProduct,
                           ProductQuery query,
                           Set<String> properties) {
        Traverser<T> traverser =
            setupTraverser(getProductId(sourceProduct), query);
        List<ImmutableProduct> res = new ArrayList<ImmutableProduct>();
        GraphCursor<T> cursor = traverser.traverse();
        try {
            while (cursor.hasNext()) {
                GraphEdge<T> edge = cursor.next();
                NodeID<T> related = edge.getEndNode();
                res.add(getProductProperties(related.getID(), properties));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    public ImmutableProduct
        getProduct(String productId, Set<String> properties) {
        T key = idFactory.fromString(productId);
        return getProductProperties(key, properties);
    }

    private ProductID<T> getProductId(String id) {
        T key = idFactory.fromString(id);
        return new ProductID<T>(key);
    }

    private ProductCache<T> setupCache(Graph<T> productGraph) {
        CacheBuilder<T, Product> builder = new CacheBuilder<T, Product>();
        builder.weigher(new Weigher<T, Product>() {
            @Override
            public int weigh(int overhead, T key, Product value) {
                return overhead +
                    40 + // estimated (maximum) key size, bytes
                    value.getWeight();
            }
        });
        // TODO: Must be configurable
        builder.maxWeight(Runtime.getRuntime().maxMemory() / 4);
        // Caching data for all nodes in the graph should suffice
        builder.maxSize(productGraph.nodeCount());
        return new ProductCacheImpl<T>(builder.build());
    }

    private Traverser<T> setupTraverser(NodeID<T> source,
                                        ProductQuery query) {
        return productGraph.prepareTraversal(source,
                                             query.getRecommendationType())
            .maxReturnedEdges(query.getLimit())
            .maxTraversedEdges(query.getMaxCursorSize())
            .maxDepth(query.getMaxRelationDistance())
            .edgeFilter(new OnlyValidFilter(query.getFilter()))
            .build();
    }

    private ImmutableProduct getProductProperties(T productId,
                                                  Set<String> properties) {
        Product cached = productCache.getProduct(productId);
        if (cached == null)
            return fetchAndCacheProduct(productId, properties);
        for (String property : properties) {
            if (!cached.containsProperty(property))
                return fetchAndCacheProduct(productId, properties);
        }
        return new ImmutableProductImpl(idFactory.toString(productId), cached);
    }

    private ImmutableProduct fetchAndCacheProduct(T productId,
                                                  Set<String> properties) {
        Map<String, Object> data = shortTermCache.get(productId);
        if (data == null)
            data = productData.getProductData(idFactory.toString(productId));
        Boolean isValidProperty = (Boolean) data.get(Product.IS_VALID_PROPERTY);
        boolean isValid =
            isValidProperty != null && isValidProperty.booleanValue();
        TableMetadata fields = productData.getProductFields();
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
        private final Set<String> properties;

        public OnlyValidFilter(ProductFilter pFilter) {
            this.pFilter = pFilter;
            this.properties =
                new HashSet<String>(pFilter.getFilterProperties());
            properties.add(Product.IS_VALID_PROPERTY);
        }

        public boolean accepts(NodeID<T> start, NodeID<T> end) {
            ImmutableProduct product =
                getProductProperties(end.getID(), properties);
            return product.isValid() && pFilter.accepts(product);
        }
    }
}
