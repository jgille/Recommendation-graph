package tests.misc;

import recng.graph.*;
import recng.index.*;
import recng.recommendations.RecommendationGraphMetadata;

public class TestGraphImporterImpl {

    public static void main(String[] args) {
        String file = args[0];
        GraphMetadata metadata = RecommendationGraphMetadata.getInstance();
        GraphBuilder<ID<String>> builder =
            new MutableGraphImpl.Builder<ID<String>>(metadata);
        GraphImporter<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, metadata) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id, NodeType nodeType) {
                    return new NodeID<ID<String>>(StringIDs.parseKey(id),
                                                  nodeType);
                }
            };
        Graph<ID<String>> graph = importer.importGraph(file);
        System.out.println("Done, imported " + graph.nodeCount()
            + " nodes and " + graph.edgeCount() + " edges");
        importer = null;
        builder = null;
    }
}