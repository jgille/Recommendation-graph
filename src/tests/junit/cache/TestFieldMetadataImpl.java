package tests.junit.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import recng.common.FieldMetadata;
import recng.common.FieldMetadataImpl;
import recng.common.Marshaller;

/**
 * Tests for {@link recng.common.FieldMetadataImpl}.
 *
 * @author Jon Ivmark
 */
public class TestFieldMetadataImpl {

    @Test public void testShort() {
        String fieldName = "f";
        FieldMetadata.Type type = FieldMetadata.Type.SHORT;
        FieldMetadata fm = new FieldMetadataImpl(fieldName, type);
        assertEquals(fieldName, fm.getFieldName());
        assertEquals(FieldMetadata.Type.SHORT, fm.getType());
        Marshaller m = fm.getMarshaller();
        Short s = (short)2;
        byte[] ba = m.marshall(s);
        assertEquals(s, m.unmarshall(ba));
    }
}
