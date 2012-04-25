package tests.junit.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.FieldType;
import recng.common.Marshaller;

/**
 * Tests for {@link recng.common.FieldMetadataImpl}.
 * 
 * @author Jon Ivmark
 */
public class TestFieldMetadataImpl {

    @Test
    public void testShort() {
        String fieldName = "f";
        FieldType type = FieldType.SHORT;
        FieldMetadata fm = FieldMetadataImpl.create(fieldName, type);
        assertEquals(fieldName, fm.getFieldName());
        assertEquals(FieldType.SHORT, fm.getType());
        Marshaller m = fm.getMarshaller();
        Short s = (short) 2;
        byte[] ba = m.marshall(s);
        assertEquals(s, m.unmarshall(ba));
    }
}
