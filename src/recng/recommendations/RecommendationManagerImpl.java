package recng.recommendations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import recng.recommendations.domain.ImmutableProduct;
import recng.recommendations.filter.ProductFilter;
import recng.recommendations.graph.RecommendationType;

/**
 * The manager interface for making recommendations.
 *
 * @author jon
 *
 */
public class RecommendationManagerImpl implements RecommendationManager {

    private static final int DEFAULT_LIMIT = 6;

    private final RecommendationModel model;

    public RecommendationManagerImpl(RecommendationModel model) {
        this.model = model;
    }

    @Override
    public List<ImmutableProduct>
        getRelatedProducts(Map<String, String> params) {

        List<String> products = getProducts(params);
        if (products == null || products.isEmpty())
            return Collections.emptyList();

        List<ProductQuery> queries = getQueries(params);
        int limit = getLimit(params);

        List<ImmutableProduct> res = new ArrayList<ImmutableProduct>();
        for (ProductQuery query : queries) {
            for (String product : products) {
                List<ImmutableProduct> related =
                    model.getRelatedProducts(product, query);
                if (related != null)
                    res.addAll(related);
                if (res.size() >= limit)
                    break;
            }
        }

        if (res.size() >= limit)
            return res.subList(0, limit);
        return res;
    }

    private List<String> getProducts(Map<String, String> params) {
        if (params.containsKey("ProductID")) {
            return Collections.singletonList(params.get("ProductID").trim());
        }
        if (params.containsKey("ProductIDs")) {
            return getListParam("ProductIDs", params);
        }
        return null;
    }

    private List<ProductQuery> getQueries(Map<String, String> params) {
        String template = getTemplateName(params);
        return getTemplateQueries(template);
    }

    private List<ProductQuery> getTemplateQueries(String templateName) {
        // TODO: Get from template
        ProductQueryImpl q0 =
            new ProductQueryImpl(5, RecommendationType.PEOPLE_WHO_BOUGHT);
        q0.setFilter(getFilter(templateName, 0));
        ProductQueryImpl q1 =
            new ProductQueryImpl(5, RecommendationType.PEOPLE_WHO_VIEWED);
        q1.setFilter(getFilter(templateName, 1));
        return Arrays.<ProductQuery> asList(q0, q1);
    }

    private ProductFilter getFilter(String templateName, int subTemplate) {


        return new ProductFilter() {
            @Override
            public boolean accepts(ImmutableProduct product) {
                return (Double) product.getProperty("Price") > 10
                    && (Double) product.getProperty("Price") <= 30;
            }
        };
    }

    private List<String> getListParam(String pName, Map<String, String> params) {
        String param = params.get(pName);
        String[] sa = param.split(",");
        List<String> res = new ArrayList<String>();
        for (String s : sa)
            res.add(s.trim());
        return res;
    }

    private int getLimit(Map<String, String> params) {
        String template = getTemplateName(params);
        return getTemplateLimit(template);
    }

    private int getTemplateLimit(String templateName) {
        return DEFAULT_LIMIT; // TODO: Check template
    }

    private String getTemplateName(Map<String, String> params) {
        String template = params.get("TemplateName");
        return template;
    }

    @Override
    public String toString() {
        return model.getStatusString();
    }
}
