package recng.predictor.graph;

import recng.graph.Graph;
import recng.predictor.PredictorService;

/**
 * A prediction service producing a graph.
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the graph node keys.
 */
public interface GraphPredictorService<T> extends PredictorService<Graph<T>> {

}
