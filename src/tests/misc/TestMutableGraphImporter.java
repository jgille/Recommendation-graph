package tests.misc;

import recng.graph.*;
import recng.index.*;
import recng.recommendations.RecommendationGraphMetadata;

public class TestMutableGraphImporter {

    public static void main(String[] args) {
        String file = args[0];
        GraphMetadata metadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            new MutableGraphImpl.Builder<ID<String>>(metadata);
        GraphImporterImpl<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, metadata) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id,
                                                        NodeType nodeType) {
                    ID<String> key = StringIDs.parseKey(id);
                    return new NodeID<ID<String>>(key, nodeType);
                }
            };
        Graph<ID<String>> graph = importer.importGraph(file);
        importer = null;
        builder = null;

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
}
