/**
 * 
 */
package recng.index;

import org.springframework.core.convert.converter.Converter;

/**
 * @author adamskogman
 * 
 */
public class StringToStringIdConverter implements Converter<String, ID<String>> {

    @Override
    public ID<String> convert(String source) {
        return StringIDs.parseID(source);
    }

}
