package rectest.graph;

import rectest.common.PropertyContainer;

public interface PropertyNode<U, V> {

    U getId();

    PropertyContainer<V> getProperties();
}
