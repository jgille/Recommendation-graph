package recng.common;

import recng.cache.Weighted;

/**
 * A property container that keeps track of it's approximate weight (size) in
 * bytes.
 * 
 * @author Jon Ivmark
 */
public interface WeightedPropertyContainer extends PropertyContainer, Weighted {

}
