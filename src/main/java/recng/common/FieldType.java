package recng.common;

import java.util.Date;

/**
 * Field type descriptor for a {@link FieldMetadata} instance.
 *
 * @author jon
 *
 */
public enum FieldType {
    BYTE {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new ByteMarshaller((Byte) defaultValue);
        }
    }, SHORT {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new ShortMarshaller((Short) defaultValue);
        }
    }, INT {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new IntegerMarshaller((Integer) defaultValue);
        }
    }, LONG {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new LongMarshaller((Long) defaultValue);
        }
    }, FLOAT {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new FloatMarshaller((Float) defaultValue);
        }
    }, DOUBLE {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new DoubleMarshaller((Double) defaultValue);
        }
    }, BOOLEAN {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new BooleanMarshaller((Boolean) defaultValue);
        }
    }, STRING {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new StringMarshaller((String) defaultValue);
        }
    }, DATE {

        @Override
        public Marshaller createMarshaller(Object defaultValue) {
            return new DateMarshaller((Date) defaultValue);
        }
    };

    public abstract Marshaller createMarshaller(Object defaultValue);

}