package recng.recommendations;

import recng.graph.NodeId;

public class ProductId<K> extends NodeId<K> {

    public ProductId(K id) {
        super(id, ProductNodeType.getInstance());
    }
}
