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
import recng.graph.NodeId;
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
public class RecommendationModelImpl<T> implements RecommendationModel<T> {

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

    public List<Product<T>>
        getRelatedProducts(String sourceProduct,
                           ProductQuery<T> query,
                           Set<String> properties) {
        Traverser<T> traverser =
            setupTraverser(getProductId(sourceProduct), query);
        List<Product<T>> res = new ArrayList<Product<T>>();
        GraphCursor<T> cursor = traverser.traverse();
        try {
            while (cursor.hasNext()) {
                GraphEdge<T> edge = cursor.next();
                NodeId<T> related = edge.getEndNode();
                res.add(getProductProperties(related.getId(), properties));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    public Product<T> getProduct(String productId, Set<String> properties) {
        T key = idFactory.fromString(productId);
        return getProductProperties(key, properties);
    }

    private ProductId<T> getProductId(String id) {
        T key = idFactory.fromString(id);
        return new ProductId<T>(key);
    }

    private ProductCache<T> setupCache(Graph<T> productGraph) {
        CacheBuilder<T, Product<T>> builder = new CacheBuilder<T, Product<T>>();
        builder.weigher(new Weigher<T, Product<T>>() {
            @Override
            public int weigh(int overhead, T key, Product<T> value) {
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

    private Traverser<T> setupTraverser(NodeId<T> source,
                                        ProductQuery<T> query) {
        return productGraph.prepareTraversal(source,
                                             query.getRecommendationType())
            .maxReturnedEdges(query.getLimit())
            .maxTraversedEdges(query.getMaxCursorSize())
            .maxDepth(query.getMaxRelationDistance())
            .edgeFilter(new OnlyValidFilter(query.getFilter()))
            .build();
    }

    private Product<T> getProductProperties(T productId,
                                            Set<String> properties) {
        Product<T> cached = productCache.getProduct(productId);
        if (cached == null)
            return fetchAndCacheProduct(productId, properties);
        for (String property : properties) {
            if (!cached.containsProperty(property))
                return fetchAndCacheProduct(productId, properties);
        }
        return cached;
    }

    private Product<T> fetchAndCacheProduct(T productId,
                                            Set<String> properties) {
        Map<String, Object> data = shortTermCache.get(productId);
        if (data == null)
            data = productData.getProductData(idFactory.toString(productId));
        Boolean isValidProperty = (Boolean) data.get(Product.IS_VALID_PROPERTY);
        boolean isValid =
            isValidProperty != null && isValidProperty.booleanValue();
        TableMetadata fields = productData.getProductFields();
        Product<T> product = new ProductImpl<T>(productId, isValid, fields);
        for (Map.Entry<String, Object> property : data.entrySet()) {
            String key = property.getKey();
            Object value = property.getValue();
            setProductProperty(product, fields.getFieldMetadata(key), value);
        }
        productCache.cacheProduct(product);
        return product;
    }

    private void setProductProperty(Product<T> product,
                                    FieldMetadata<?> field,
                                    Object value) {
        if (field.isRepeated()) {
            setRepeatedProductProperty(product, field, value);
            return;
        }
        product.setProperty(field.getFieldName(), value);
    }

    private void setRepeatedProductProperty(Product<T> product,
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

        private final ProductFilter<T> pFilter;
        private final Set<String> properties;

        public OnlyValidFilter(ProductFilter<T> pFilter) {
            this.pFilter = pFilter;
            this.properties =
                new HashSet<String>(pFilter.getFilterProperties());
            properties.add(Product.IS_VALID_PROPERTY);
        }

        public boolean accepts(NodeId<T> start, NodeId<T> end) {
            Product<T> product =
                getProductProperties(end.getId(), properties);
            return product.isValid() && pFilter.accepts(product);
        }
    }
}
