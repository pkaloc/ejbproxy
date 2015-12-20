package ejbproxy.runtime.configuration;

import javax.management.NotificationEmitter;
import java.util.List;

public interface RuntimeConfigurationMBean extends NotificationEmitter {
	// --- Global config ---

	boolean isEJBProxyEnabled();

	void setEJBProxyEnabled(boolean enabled, boolean resetExisting);

	List<String> getDefaultEndpoints();

	void setDefaultEndpoints(List<String> endpoints, boolean resetExisting);

	String getDefaultHandler();

	void setDefaultHandler(String handler, boolean resetExisting);

	boolean isLoggingEnabled();

	void setLoggingEnabled(boolean enabled, boolean resetExisting);

	String getDefaultLogger();

	void setDefaultLogger(String logger, boolean resetExisting);
	
	String getDefaultLoggerName();
	
	void setDefaultLoggerName(String loggerName, boolean resetExisting);

	// --- Concrete proxy config ---
	// If null value is returned, the default configuration applies for the
	// given ejb.

	Boolean isEJBProxyEnabled(String jndiBinding);

	void setEJBProxyEnabled(String jndiBinding, boolean enabled);

	List<String> getEndpoints(String jndiBinding);

	void setEndpoints(String jndiBinding, List<String> endpoints);

	String getHandler(String jndiBinding);

	void setHandler(String jndiBinding, String handler);

	Boolean isLoggingEnabled(String jndiBinding);

	void setLoggingEnabled(String jndiBinding, boolean enabled);

	String getLogger(String jndiBinding);

	void setLogger(String jndiBinding, String logger);
	
	String getLoggerName(String jndiBinding);
	
	void setLoggerName(String jndiBinding, String loggerName);
}
