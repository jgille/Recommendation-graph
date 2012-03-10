package recng.common;

/**
 * Default marshallers for all supported primitives. All marshallers are thread
 * safe.
 * 
 * @author Jon Ivmark
 */
public class DefaultMarshallers {

    private DefaultMarshallers() {
        // Prevent instantiation
    }

    public static final Marshaller BYTE_MARSHALLER =
        new ByteMarshaller(null);

    public static final Marshaller SHORT_MARSHALLER =
        new ShortMarshaller(null);

    public static final Marshaller INTEGER_MARSHALLER =
        new IntegerMarshaller(null);

    public static final Marshaller LONG_MARSHALLER =
        new LongMarshaller(null);

    public static final Marshaller DATE_MARSHALLER =
        new DateMarshaller(null);

    public static final Marshaller FLOAT_MARSHALLER =
        new FloatMarshaller(null);

    public static final Marshaller DOUBLE_MARSHALLER =
        new DoubleMarshaller(null);

    public static final Marshaller BOOLEAN_MARSHALLER =
        new BooleanMarshaller(null);

    public static final Marshaller STRING_MARSHALLER =
        new StringMarshaller(null);
}
