package ejbproxy.runtime.configuration.web.util;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;

@SuppressWarnings("unchecked")
public class RuntimeConfigurationOperationInvoker {
	private final ObjectName configJmxName;
	private final MBeanServer mBeanServer;

	public RuntimeConfigurationOperationInvoker(ObjectName configJmxName)
			throws Exception {
		if (configJmxName == null) {
			throw new NullPointerException();
		}

		this.configJmxName = configJmxName;
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
	}

	public List<String> getDefaultEndpoints() throws Exception {
		return (List<String>) mBeanServer.invoke(configJmxName,
				"getDefaultEndpoints", null, null);
	}

	public String getDefaultHandler() throws Exception {
		return (String) mBeanServer.invoke(configJmxName, "getDefaultHandler",
				null, null);
	}

	public String getDefaultLogger() throws Exception {
		return (String) mBeanServer.invoke(configJmxName, "getDefaultLogger",
				null, null);
	}

	public String getDefaultLoggerName() throws Exception {
		return (String) mBeanServer.invoke(configJmxName,
				"getDefaultLoggerName", null, null);
	}

	public List<String> getEndpoints(String jndiBinding) throws Exception {
		return (List<String>) mBeanServer.invoke(configJmxName, "getEndpoints",
				new Object[] { jndiBinding },
				new String[] { String.class.getName() });
	}

	public String getHandler(String jndiBinding) throws Exception {
		return (String) mBeanServer.invoke(configJmxName, "getHandler",
				new Object[] { jndiBinding },
				new String[] { String.class.getName() });
	}

	public String getLogger(String jndiBinding) throws Exception {
		return (String) mBeanServer.invoke(configJmxName, "getLogger",
				new Object[] { jndiBinding },
				new String[] { String.class.getName() });
	}

	public String getLoggerName(String jndiBinding) throws Exception {
		return (String) mBeanServer.invoke(configJmxName, "getLoggerName",
				new Object[] { jndiBinding },
				new String[] { String.class.getName() });
	}

	public boolean isEJBProxyEnabled() throws Exception {
		return (Boolean) mBeanServer.invoke(configJmxName, "isEJBProxyEnabled",
				null, null);
	}

	public Boolean isEJBProxyEnabled(String jndiBinding) throws Exception {
		return (Boolean) mBeanServer.invoke(configJmxName, "isEJBProxyEnabled",
				new Object[] { jndiBinding },
				new String[] { String.class.getName() });
	}

	public boolean isLoggingEnabled() throws Exception {
		return (Boolean) mBeanServer.invoke(configJmxName, "isLoggingEnabled",
				null, null);
	}

	public Boolean isLoggingEnabled(String jndiBinding) throws Exception {
		return (Boolean) mBeanServer.invoke(configJmxName, "isLoggingEnabled",
				new Object[] { jndiBinding },
				new String[] { String.class.getName() });
	}

	public void setDefaultEndpoints(List<String> endpoints,
			boolean resetExisting) throws Exception {
		mBeanServer
				.invoke(configJmxName,
						"setDefaultEndpoints",
						new Object[] { endpoints, resetExisting },
						new String[] { List.class.getName(),
								boolean.class.getName() });
	}

	public void setDefaultHandler(String handler, boolean resetExisting)
			throws Exception {
		mBeanServer.invoke(configJmxName, "setDefaultHandler", new Object[] {
				handler, resetExisting }, new String[] {
				String.class.getName(), boolean.class.getName() });
	}

	public void setDefaultLogger(String logger, boolean resetExisting)
			throws Exception {
		mBeanServer.invoke(configJmxName, "setDefaultLogger", new Object[] {
				logger, resetExisting }, new String[] { String.class.getName(),
				boolean.class.getName() });
	}

	public void setDefaultLoggerName(String loggerName, boolean resetExisting)
			throws Exception {
		mBeanServer
				.invoke(configJmxName, "setDefaultLoggerName", new Object[] {
						loggerName, resetExisting }, new String[] {
						String.class.getName(), boolean.class.getName() });
	}

	public void setEJBProxyEnabled(boolean enabled, boolean resetExisting)
			throws Exception {
		mBeanServer
				.invoke(configJmxName,
						"setEJBProxyEnabled",
						new Object[] { enabled, resetExisting },
						new String[] { boolean.class.getName(),
								boolean.class.getName() });
	}

	public void setEJBProxyEnabled(String jndiBinding, boolean enabled)
			throws Exception {
		mBeanServer.invoke(configJmxName, "setEJBProxyEnabled", new Object[] {
				jndiBinding, enabled }, new String[] { String.class.getName(),
				boolean.class.getName() });
	}

	public void setEndpoints(String jndiBinding, List<String> endpoints)
			throws Exception {
		mBeanServer.invoke(configJmxName, "setEndpoints", new Object[] {
				jndiBinding, endpoints }, new String[] {
				String.class.getName(), List.class.getName() });
	}

	public void setHandler(String jndiBinding, String handler) throws Exception {
		mBeanServer.invoke(configJmxName, "setHandler", new Object[] {
				jndiBinding, handler }, new String[] { String.class.getName(),
				String.class.getName() });
	}

	public void setLogger(String jndiBinding, String logger) throws Exception {
		mBeanServer.invoke(configJmxName, "setLogger", new Object[] {
				jndiBinding, logger }, new String[] { String.class.getName(),
				String.class.getName() });
	}

	public void setLoggerName(String jndiBinding, String loggerName)
			throws Exception {
		mBeanServer
				.invoke(configJmxName,
						"setLoggerName",
						new Object[] { jndiBinding, loggerName },
						new String[] { String.class.getName(),
								String.class.getName() });
	}

	public void setLoggingEnabled(boolean enabled, boolean resetExisting)
			throws Exception {
		mBeanServer
				.invoke(configJmxName,
						"setLoggingEnabled",
						new Object[] { enabled, resetExisting },
						new String[] { boolean.class.getName(),
								boolean.class.getName() });
	}

	public void setLoggingEnabled(String jndiBinding, boolean enabled)
			throws Exception {
		mBeanServer.invoke(configJmxName, "setLoggingEnabled", new Object[] {
				jndiBinding, enabled }, new String[] { String.class.getName(),
				boolean.class.getName() });
	}
}
