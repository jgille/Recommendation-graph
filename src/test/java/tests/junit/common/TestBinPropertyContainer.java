package tests.junit.common;

import recng.common.BinPropertyContainer;
import recng.common.TableMetadata;
import recng.common.PropertyContainer;

/**
 * Tests {@link BinPropertyContainer}.
 * 
 * @author jon
 * 
 */
public class TestBinPropertyContainer extends
    AbstractTestPropertyContainer {

    @Override
    protected PropertyContainer getPropertyContainer(TableMetadata fs) {
        return new BinPropertyContainer.Factory(true).create(fs);
    }
}
