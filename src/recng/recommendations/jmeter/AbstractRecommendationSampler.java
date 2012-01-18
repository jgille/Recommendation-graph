package recng.recommendations.jmeter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import recng.recommendations.RecommendationManager;
import recng.recommendations.domain.ImmutableProduct;

/**
 * A simple {@link AbstractJavaSamplerClient} used to load test a
 * {@link RecommendationManager}.
 *
 * @author jon
 *
 */
public abstract class AbstractRecommendationSampler extends AbstractJavaSamplerClient {


    @Override
    public SampleResult runTest(JavaSamplerContext ctx) {
        SampleResult sr = new SampleResult();
        sr.sampleStart();
        Map<String, String> params =
            Collections.singletonMap("ProductID", getNextProduct());
        List<ImmutableProduct> rec = getManager().getRelatedProducts(params);
        sr.setResponseMessage(rec + "");
        sr.sampleEnd();
        sr.setSuccessful(true);
        return sr;
    }

    /**
     * Gets the manager to use.
     */
    protected abstract RecommendationManager getManager();

    /**
     * Gets the next product to use as parameter.
     */
    protected abstract String getNextProduct();
}