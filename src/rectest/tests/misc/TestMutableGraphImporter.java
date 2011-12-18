package rectest.tests.misc;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import rectest.graph.*;
import rectest.index.*;
import rectest.recommendations.RecommendationType;

public class TestMutableGraphImporter {

    private static final Set<EdgeType> EDGE_TYPES =
        new HashSet<EdgeType>(
                              Arrays
                                  .asList(RecommendationType.PEOPLE_WHO_BOUGHT,
                                          RecommendationType.PEOPLE_WHO_VIEWED));
    private static final NodeType NODE_TYPE =
        new NodeTypeImpl("Product", EDGE_TYPES);

    public static void main(String[] args) {
        String file = args[0];
        Set<EdgeType> edgeTypes = new HashSet<EdgeType>();
        GraphBuilder<Key<String>> builder =
            new MutableGraphImpl.Builder<Key<String>>(edgeTypes);
        edgeTypes.addAll(EnumSet.allOf(RecommendationType.class));
        GraphImporterImpl<Key<String>> importer =
            new GraphImporterImpl<Key<String>>(builder, edgeTypes) {

                @Override
                protected NodeId<Key<String>> getNodeKey(String id) {
                    Key<String> key = StringKeys.parseKey(id);
                    return new NodeId<Key<String>>(key, NODE_TYPE);
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
