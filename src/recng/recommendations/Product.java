package recng.recommendations;

import java.util.List;

import recng.common.WeightedPropertyContainer;

public interface Product<K> extends WeightedPropertyContainer<String> {

    K getId();

    boolean isValid();

    void setIsValid(boolean isValid);

    List<String> getCategories();

    void setCategories(List<String> categories);
}
