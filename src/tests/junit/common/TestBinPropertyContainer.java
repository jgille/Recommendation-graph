package tests.junit.common;

import rectest.common.BinPropertyContainer;
import rectest.common.FieldSet;
import rectest.common.PropertyContainer;

public class TestBinPropertyContainer extends
    AbstractTestPropertyContainer {

    @Override
    protected PropertyContainer<String> getPropertyContainer(FieldSet fs) {
        return BinPropertyContainer.build(fs, true);
    }
}
