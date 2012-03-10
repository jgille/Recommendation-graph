package recng.common;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Date marshaller.
 *
 * @author jon
 *
 */
public class DateMarshaller extends AbstractMarshaller<Date> {

    private final DateFormat dateFormat;

    public DateMarshaller(Date defaultValue) {
        super(defaultValue);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Override
    public byte[] marshall(Object value) {
        return ByteBuffer.allocate(8).putLong(getTypedValue(value).getTime())
            .array();
    }

    @Override
    public Date unmarshall(byte[] bytes) {
        checkByteArrayLength(bytes, 8);
        return new Date(ByteBuffer.wrap(bytes).getLong());
    }

    @Override
    public Date parse(String s) {
        if (s.isEmpty())
            return getDefaultValue();
        try {
            synchronized (dateFormat) {
                return dateFormat.parse(s);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
