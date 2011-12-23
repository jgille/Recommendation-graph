package tests.junit.common;

import recng.common.BinPropertyContainer;
import recng.common.TableMetadata;
import recng.common.PropertyContainer;

public class TestBinPropertyContainer extends
    AbstractTestPropertyContainer {

    @Override
    protected PropertyContainer getPropertyContainer(TableMetadata fs) {
        return new BinPropertyContainer(fs, true);
    }
}
