package rectest.common;

import rectest.cache.Weighted;

/**
 * A property container that keeps track of it's approximate weight (size) in bytes.
 *
 * @author Jon Ivmark
 */
public interface WeightedPropertyContainer<K> extends PropertyContainer<K>, Weighted {

}
