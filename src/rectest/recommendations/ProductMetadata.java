package rectest.recommendations;

import java.util.Map;
import rectest.common.FieldSet;

public interface ProductMetadata {

    public static final String IS_VALID_KEY = "__is_valid";
    public static final String CATEGORIES_KEY = "__categories";

    Map<String, Object> getProductMetadata(String productId);

    FieldSet getProductFields();
}
