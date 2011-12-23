package tests.misc;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import recng.graph.*;
import recng.index.*;
import recng.recommendations.RecommendationType;

public class TestMutableGraphImporter {

    private static final List<EdgeType> EDGE_TYPES =
        Arrays.<EdgeType>asList(RecommendationType.PEOPLE_WHO_BOUGHT,
                                 RecommendationType.PEOPLE_WHO_VIEWED);
    private static final NodeType NODE_TYPE =
        new NodeTypeImpl("Product", EDGE_TYPES);

    public static void main(String[] args) {
        String file = args[0];
        Set<EdgeType> edgeTypes = new HashSet<EdgeType>();
        GraphBuilder<ID<String>> builder =
            new MutableGraphImpl.Builder<ID<String>>(edgeTypes);
        edgeTypes.addAll(EnumSet.allOf(RecommendationType.class));
        GraphImporterImpl<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(builder, edgeTypes) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id) {
                    ID<String> key = StringIDs.parseKey(id);
                    return new NodeID<ID<String>>(key, NODE_TYPE);
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
