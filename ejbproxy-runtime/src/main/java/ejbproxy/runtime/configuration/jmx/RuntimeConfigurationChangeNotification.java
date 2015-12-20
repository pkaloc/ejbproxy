package ejbproxy.runtime.configuration.jmx;

import javax.management.Notification;

public class RuntimeConfigurationChangeNotification extends Notification {
	private static final long serialVersionUID = 1L;

	public enum ConfigurationElement {
		EJBPROXY_ENABLED, ENDPOINTS, HANDLER, LOGGING_ENABLED, LOGGER, LOGGER_NAME
	}

	public static final String DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION = "ejbproxy.configuration.default.change";
	public static final String SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION = "ejbproxy.configuration.specific.change";

	private final String jndiBinding;
	private final ConfigurationElement element;
	private final Object oldValue;
	private final Object newValue;

	public RuntimeConfigurationChangeNotification(String type, Object source,
			long sequenceNumber, ConfigurationElement element, Object oldValue,
			Object newValue) {
		super(type, source, sequenceNumber);
		jndiBinding = null;
		this.element = element;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public RuntimeConfigurationChangeNotification(String type, Object source,
			long sequenceNumber, String jndiBinding,
			ConfigurationElement element, Object oldValue, Object newValue) {
		super(type, source, sequenceNumber);
		this.jndiBinding = jndiBinding;
		this.element = element;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getJndiBinding() {
		return jndiBinding;
	}

	public ConfigurationElement getElement() {
		return element;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}
}
