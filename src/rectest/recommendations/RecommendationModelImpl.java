package rectest.recommendations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rectest.cache.Cache;
import rectest.cache.CacheBuilder;
import rectest.common.FieldMetadata;
import rectest.common.FieldSet;
import rectest.common.filter.ProductFilter;
import rectest.graph.NodeId;
import rectest.graph.EdgeFilter;
import rectest.graph.Graph;
import rectest.graph.GraphEdge;
import rectest.graph.GraphCursor;
import rectest.graph.Traverser;

public class RecommendationModelImpl<K> implements RecommendationModel<K> {

    // The product graph, i.e. the relations between products
    private final Graph<K> productGraph;
    // The backend (db) storage of product metadata
    private final ProductMetadata pmd;
    // Cached product metadata
    private final ProductMetadataCache<K> pmdCache;
    // Short lived cache used to avoid making duplicate db requests for the same
    // product in close succession
    private final Cache<K, Map<String, Object>> shortTermCache =
        new CacheBuilder<K, Map<String, Object>>()
            .maxSize(1000).build();
    // Converts a string representation of a product id to an internal
    // representation of the id.
    private final KeyParser<K> keyParser;

    public RecommendationModelImpl(Graph<K> productGraph,
                                   ProductMetadata productMetadata,
                                   ProductMetadataCache<K> productMetadataCache,
                                   KeyParser<K> keyParser) {
        this.productGraph = productGraph;
        this.pmd = productMetadata;
        this.pmdCache = productMetadataCache;
        this.keyParser = keyParser;
    }

    public List<Product<K>>
        getRelatedProducts(String sourceProduct,
                           ProductQuery<K> query,
                           Set<String> properties) {
        Traverser<K> traverser =
            setupTraverser(getProductId(sourceProduct), query);
        List<Product<K>> res = new ArrayList<Product<K>>();
        GraphCursor<K> cursor = traverser.traverse();
        try {
            while (cursor.hasNext()) {
                GraphEdge<K> edge = cursor.next();
                NodeId<K> related = edge.getEndNode();
                res.add(getProductProperties(related.getId(), properties));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    public Product<K> getProduct(String productId, Set<String> properties) {
        K key = keyParser.parseKey(productId);
        return getProductProperties(key, properties);
    }

    private ProductId<K> getProductId(String id) {
        K key = keyParser.parseKey(id);
        return new ProductId<K>(key);
    }

    private Traverser<K>
        setupTraverser(NodeId<K> source, ProductQuery<K> query) {
        return productGraph.prepareTraversal(source,
                                             query.getRecommendationType())
            .maxReturnedEdges(query.getLimit())
            .maxTraversedEdges(query.getMaxCursorSize())
            .maxDepth(query.getMaxRelationDistance())
            .edgeFilter(new Filter(query.getFilter()))
            .build();
    }

    private class Filter implements EdgeFilter<K> {

        private final ProductFilter<K> pFilter;
        private final Set<String> properties;

        public Filter(ProductFilter<K> pFilter) {
            this.pFilter = pFilter;
            this.properties = new HashSet<String>(pFilter.getFilterProperties());
            properties.add(FieldMetadata.IS_VALID);
        }

        public boolean accepts(NodeId<K> start, NodeId<K> end) {
            Product<K> product =
                getProductProperties(end.getId(), properties);
            return product.isValid() && pFilter.accepts(product);
        }
    }

    private Product<K> getProductProperties(K productId,
                                            Set<String> properties) {
        Product<K> cached = pmdCache.getProduct(productId);
        if (cached == null)
            return fetchAndCacheProduct(productId, properties);
        for (String property : properties) {
            if (!cached.containsProperty(property))
                return fetchAndCacheProduct(productId, properties);
        }
        return cached;
    }

    private Product<K> fetchAndCacheProduct(K productId,
                                            Set<String> properties) {
        Map<String, Object> metadata = shortTermCache.get(productId);
        if (metadata == null)
            metadata = pmd.getProductMetadata(keyParser.toString(productId));
        Boolean isValid = (Boolean) metadata.get(ProductMetadata.IS_VALID_KEY);
        FieldSet fields = pmd.getProductFields();
        Product<K> product =
            new ProductImpl<K>(productId, isValid == null
                || isValid.booleanValue(), fields);
        for (Map.Entry<String, Object> property : metadata.entrySet()) {
            String key = property.getKey();
            Object value = property.getValue();
            setProductProperty(product, fields.getFieldMetadata(key), value);
        }
        return product;
    }

    private void setProductProperty(Product<K> product,
                                    FieldMetadata<?> field,
                                    Object value) {
        if (field.isRepeated()) {
            setRepeatedProductProperty(product, field, value);
            return;
        }
        product.setProperty(field.getFieldName(), value);
    }

    private void setRepeatedProductProperty(Product<K> product,
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
}
