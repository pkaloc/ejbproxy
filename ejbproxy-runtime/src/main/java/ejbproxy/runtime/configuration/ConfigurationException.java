package ejbproxy.runtime.configuration;

import ejbproxy.runtime.EJBProxyRuntimeException;

/**
 * Represents an exception which may be thrown by {@link Configurable} to advice
 * caller that configuration seriously failed and so will not work normally.
 */
public class ConfigurationException extends EJBProxyRuntimeException {
	private static final long serialVersionUID = 1L;

	public ConfigurationException() {
		super();
	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}
}
