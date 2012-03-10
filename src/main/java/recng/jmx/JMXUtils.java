package recng.jmx;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.mahout.math.map.AbstractObjectIntMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

public class JMXUtils {

    private static final String DEFAULT_DOMAIN = "recng";

    private static final AbstractObjectIntMap<String> M_BEAN_NAMES =
        new OpenObjectIntHashMap<String>();

    public static void registerMBean(Object mBean) {
        String mBeanName = mBean.getClass().getSimpleName();
        synchronized (M_BEAN_NAMES) {
            int cnt = M_BEAN_NAMES.adjustOrPutValue(mBeanName, 1, 1);
            mBeanName = String.format("%s(%d)", mBeanName, cnt);
        }
        registerMBean(mBean, DEFAULT_DOMAIN, Collections.singletonMap("name", mBeanName));
    }

    public static void registerMBean(Object mBean, String mBeanDomain,
                                     Map<String, String> mBeanProperties) {
        try {
            StringBuilder mBeanObjectPattern = new StringBuilder(mBeanDomain).append(":");
            boolean first = true;
            for (Map.Entry<String, String> e : mBeanProperties.entrySet()) {
                if (!first)
                    mBeanObjectPattern.append(", ");
                first = true;
                String key = e.getKey();
                String property = e.getValue();
                mBeanObjectPattern.append(key).append("=").append(property);
            }
            ObjectName oName = new ObjectName(mBeanObjectPattern.toString());
            MBeanServer server = getServer();
            server.registerMBean(mBean, oName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InstanceAlreadyExistsException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static MBeanServer getServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

}
