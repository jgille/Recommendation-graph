package recng.recommendations.graph;

import recng.graph.Graph;

/**
 * Used to create a recommendation graph, i.e. a graph containing product nodes
 * connected by weighted edges representing the relation between products, for
 * instance "other people who bought this product also bought these products.".
 *
 * @author jon
 *
 * @param <K>
 */
public interface Predictor<K> {

    /**
     * Creates the product graph.
     * 
     * @return
     */
    Graph<K> setupPredictions();
}
