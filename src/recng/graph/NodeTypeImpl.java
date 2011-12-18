package recng.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Describes a type of node in a graph.
 *
 * @author jon
 *
 */
public class NodeTypeImpl implements NodeType {

    private final String name;
    private final Map<EdgeType, Integer> validEdgeTypes =
        new HashMap<EdgeType, Integer>();

    /**
     * Creates a node type instance.
     *
     * @param name
     *            The node type name
     * @param validEdgeTypes
     *            The valid edge types of edges originating from nodes of this
     *            type
     */
    public NodeTypeImpl(String name, List<EdgeType> validEdgeTypes) {
        this.name = name;
        int i = 0;
        for (EdgeType et : validEdgeTypes)
            this.validEdgeTypes.put(et, i++);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Map<EdgeType, Integer> validEdgeTypes() {
        return validEdgeTypes;
    }

    @Override
    public int indexOf(EdgeType edgeType) {
        if (!validEdgeTypes.containsKey(edgeType))
            return -1;
        return validEdgeTypes.get(edgeType);
    }
}
