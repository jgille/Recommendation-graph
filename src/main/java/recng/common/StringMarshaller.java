package recng.common;

import java.io.UnsupportedEncodingException;

/**
 * A String marshaller.
 * 
 * @author jon
 * 
 */
public class StringMarshaller extends AbstractMarshaller<String> {

    public StringMarshaller(String defaultValue) {
        super(defaultValue);
    }

    @Override
    public byte[] marshall(Object value) {
        try {
            return (getTypedValue(value)).getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public String unmarshall(byte[] bytes) {
        if (bytes == null)
            throw new IllegalArgumentException("Null bytes not allowed");
        try {
            return new String(bytes, "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public String parse(String s) {
        return s;
    }
}
