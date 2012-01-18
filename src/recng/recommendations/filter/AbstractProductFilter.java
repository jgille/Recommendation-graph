package recng.recommendations.filter;

import java.util.Set;

public abstract class AbstractProductFilter implements ProductFilter {

    private final Set<String> fields;

    public AbstractProductFilter(Set<String> fields) {
        this.fields = fields;
    }
}