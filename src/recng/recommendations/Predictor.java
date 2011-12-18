package recng.recommendations;

import recng.graph.Graph;
import recng.index.Key;

public interface Predictor {

    Graph<Key<String>> setupPredictions(String npDataFile,
                                        String clickDataFile,
                                        KeyParser<Key<String>> pip);
}
