package recng.graph;

import recng.common.Procedure;

/**
 * A procedure accepting {@link GraphEdge}s as input.
 * 
 * @author jon
 *
 * @param <T>
 *            The generic type of the edge node ids.
 */
public interface GraphEdgeProcedure<T> extends Procedure<GraphEdge<T>> {

}
