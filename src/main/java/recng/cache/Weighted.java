package recng.cache;

/**
 * Classes that can return their own weight, often but not necessarily in bytes,
 * should imlement this interface.
 * 
 * @author Jon Ivmark
 */
public interface Weighted {

    /**
     * Gets the weight of this object.
     */
    int getWeight();
}
