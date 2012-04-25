package recng.jmx;

import java.util.Map;

public interface MBean {

    String getBeanName();

    String getDomain();

    Map<String, String> getProperties();
}
