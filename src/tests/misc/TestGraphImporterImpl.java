package tests.misc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import recng.graph.*;
import recng.index.*;
import recng.recommendations.RecommendationType;

public class TestGraphImporterImpl {

    private static final List<EdgeType> EDGE_TYPES =
        new ArrayList<EdgeType>(EnumSet.allOf(RecommendationType.class));

    private static final NodeType NODE_TYPE = new NodeTypeImpl("Product",
                                                               EDGE_TYPES);

    public static void main(String[] args) {
        String file = args[0];
        GraphBuilder<ID<String>> builder =
            new GraphImpl.Builder<ID<String>>();
        Set<EdgeType> edgeTypes = new HashSet<EdgeType>();
        edgeTypes.addAll(EnumSet.allOf(RecommendationType.class));
        GraphImporterImpl<ID<String>> importer =
            new GraphImporterImpl<ID<String>>(
                                               builder,
                                               edgeTypes) {

                @Override
                protected NodeID<ID<String>> getNodeKey(String id) {
                    return new NodeID<ID<String>>(StringIDs.parseKey(id),
                                                   NODE_TYPE);
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