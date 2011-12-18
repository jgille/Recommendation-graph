package tests.junit.common;

import recng.common.BinPropertyContainer;
import recng.common.FieldSet;
import recng.common.PropertyContainer;

public class TestBinPropertyContainer extends
    AbstractTestPropertyContainer {

    @Override
    protected PropertyContainer<String> getPropertyContainer(FieldSet fs) {
        return BinPropertyContainer.build(fs, true);
    }
}
