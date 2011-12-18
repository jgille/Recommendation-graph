package tests.junit.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import rectest.common.FieldMetadata;
import rectest.common.FieldMetadataImpl;
import rectest.common.Marshaller;
import rectest.common.Marshallers;

/**
 * Tests for {@link rectest.common.FieldMetadataImpl}.
 *
 * @author Jon Ivmark
 */
public class TestFieldMetadataImpl {

    @Test public void testShort() {
        String fieldName = "f";
        Marshaller<Short> marshaller = Marshallers.SHORT_MARSHALLER;
        FieldMetadata.Type type = FieldMetadata.Type.SHORT;
        FieldMetadata<Short> fm = new FieldMetadataImpl<Short>(fieldName, marshaller, type);
        assertEquals(fieldName, fm.getFieldName());
        assertEquals(FieldMetadata.Type.SHORT, fm.getType());
        Marshaller<Short> m = fm.getMarshaller();
        assertEquals(marshaller, m);
        Short s = (short)2;
        byte[] ba = m.marshall(s);
        assertEquals(s, m.unmarshall(ba));
    }
}
