package ejbproxy.runtime.jboss4;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

public class JBossRuntime {
	public static String getVersionString() {
		try {
			MBeanServerConnection server = (MBeanServerConnection) new InitialContext()
					.lookup("jmx/rmi/RMIAdaptor");
			ObjectName on = new ObjectName("jboss.system", "type", "Server");
			return server.getAttribute(on, "VersionNumber").toString();
		} catch (Exception e) {
			return null;
		}
	}
}
