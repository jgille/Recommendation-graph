package recng.predictor;

import java.util.Map;

/**
 * A service for creating predictions.
 * 
 * @author jon
 * 
 * @param <T>
 *            The generic type of the predictions container.
 */
public interface PredictorService<T> {

    /**
     * Initiates the service with a set of config params.
     */
    void init(Map<String, String> config);

    /**
     * Performs the prediction and returns the result.
     */
    T createPredictions();
}
