package recng.predictor;

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.AbstractIntSet;

/**
 * Base data for a prediction service.
 * 
 * @author jon
 * 
 */
public interface PredictorBaseData {

    /**
     * Get's the entire set of products.
     */
    IntArrayList getAllProducts();

    /**
     * Gets the user IDs of all the users that have purchased a product.
     */
    AbstractIntSet getBuyers(int productID);

    /**
     * Gets the session IDs of all the sessions that have viewed a product.
     */
    AbstractIntSet getViewers(int productID);

    /**
     * Gets the product IDs of all the products a user has bought.
     */
    AbstractIntSet getPurchasedProducts(int userID);

    /**
     * Gets the product IDs of all the products a session has viewed.
     */
    AbstractIntSet getViewedProducts(int sessionID);

    /**
     * Gets the product id for the product with the given index.
     */
    String getProductID(int index);

    /**
     * Gets the user id for the user with the given index.
     */
    String getUserID(int index);

    /**
     * Gets the session id for the session with the given index.
     */
    String getSessionID(int index);
}
