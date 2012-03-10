package tests.junit.common;

import recng.common.ArrayPropertyContainer;
import recng.common.TableMetadata;
import recng.common.PropertyContainer;

/**
 * Tests {@link ArrayPropertyContainer}.
 * 
 * @author jon
 * 
 */
public class TestArrayPropertyContainer extends
    AbstractTestPropertyContainer {

    @Override
    protected PropertyContainer getPropertyContainer(TableMetadata fs) {
        return new ArrayPropertyContainer(fs);
    }
}
