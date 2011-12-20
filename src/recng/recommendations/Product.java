package recng.recommendations;

import java.util.List;

import recng.common.WeightedPropertyContainer;

public interface Product<K> extends WeightedPropertyContainer<String> {

    public static final String IS_VALID_PROPERTY = "__IS_VALID";
    public static final String CATEGORIES_PROPERTY = "__CATEGORIES";

    K getId();

    boolean isValid();

    void setIsValid(boolean isValid);

    List<String> getCategories();

    void setCategories(List<String> categories);
}
