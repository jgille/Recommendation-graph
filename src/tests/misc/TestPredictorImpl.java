package tests.misc;

import java.io.*;

import recng.graph.Graph;
import recng.graph.GraphExporter;
import recng.graph.GraphExporterImpl;
import recng.graph.NodeId;
import recng.index.Key;
import recng.index.StringKeys;
import recng.recommendations.*;

public class TestPredictorImpl {

    public static void main(String[] args) {
        KeyParser<Key<String>> pip =
            new KeyParser<Key<String>>() {
            @Override
            public String toString(Key<String> productId) {
                return productId.getValue();
            }

            @Override
            public Key<String> parseKey(String id) {
                return StringKeys.parseKey(id);
            }
        };
        Graph<Key<String>> graph = new PredictorImpl().setupPredictions(args[0], args[1],
                                                                        pip);
        try {
            export(graph, "/tmp/graph");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            System.out.println("Done. Node count: " + graph.nodeCount()
                               + ", edge count " + graph.edgeCount());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void export(Graph<Key<String>> graph, String outFile)
        throws IOException {
        GraphExporter<Key<String>> exporter =
            new GraphExporterImpl<Key<String>>() {
                @Override
                protected String serialize(NodeId<Key<String>> node) {
                    return node.getId().getValue();
                }
            };
        exporter.exportGraph(graph, outFile);
    }
}
