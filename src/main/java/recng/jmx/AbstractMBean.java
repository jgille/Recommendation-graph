package recng.jmx;

import java.util.HashMap;
import java.util.Map;

public class AbstractMBean implements MBean {

    private static final String DEFAULT_DOMAIN = "recng";
    private final Map<String, String> properties;
    private String beanName;

    protected AbstractMBean() {
        this.properties = new HashMap<String, String>();
        this.beanName = getClass().getSimpleName();
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    protected void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<String, String>(properties);
    }

    protected void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    @Override
    public String getDomain() {
        return DEFAULT_DOMAIN;
    }
}
