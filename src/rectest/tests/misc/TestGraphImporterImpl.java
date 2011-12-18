package rectest.tests.misc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rectest.graph.*;
import rectest.index.*;
import rectest.recommendations.RecommendationType;

public class TestGraphImporterImpl {

    private static final List<EdgeType> EDGE_TYPES =
        new ArrayList<EdgeType>(EnumSet.allOf(RecommendationType.class));

    private static final NodeType NODE_TYPE = new NodeTypeImpl("Product",
                                                               EDGE_TYPES);

    public static void main(String[] args) {
        String file = args[0];
        GraphBuilder<Key<String>> builder =
            new GraphImpl.Builder<Key<String>>();
        Set<EdgeType> edgeTypes = new HashSet<EdgeType>();
        edgeTypes.addAll(EnumSet.allOf(RecommendationType.class));
        GraphImporterImpl<Key<String>> importer =
            new GraphImporterImpl<Key<String>>(
                                               builder,
                                               edgeTypes) {

                @Override
                protected NodeId<Key<String>> getNodeKey(String id) {
                    return new NodeId<Key<String>>(StringKeys.parseKey(id),
                                                   NODE_TYPE);
                }
            };
        Graph<Key<String>> graph = importer.importGraph(file);
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