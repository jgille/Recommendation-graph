package rectest.recommendations;

import rectest.graph.Graph;
import rectest.index.Key;

public interface Predictor {

    Graph<Key<String>> setupPredictions(String npDataFile,
                                        String clickDataFile,
                                        KeyParser<Key<String>> pip);
}
