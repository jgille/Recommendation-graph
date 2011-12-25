package recng.graph;

/**
 * Describes a type of node in a graph.
 *
 * @author jon
 *
 */
public class NodeTypeImpl implements NodeType {

    private final String name;
    private final int ordinal;
    /**
     * Creates a node type instance.
     *
     * @param name
     *            The node type name
     * @param validEdgeTypes
     *            The valid edge types of edges originating from nodes of this
     *            type
     */
    public NodeTypeImpl(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }
}
