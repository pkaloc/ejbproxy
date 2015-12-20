package ejbproxy.runtime.configuration;

import java.util.Map;

/**
 * Represents configurable objects.
 */
public interface Configurable {
	/**
	 * Configures object.
	 * <p>
	 * Called every time a new configuration is ready for object. In most cases
	 * once only, upon object creation.
	 * 
	 * @param configuration
	 *            new configuration. It is never <code>null</code>, but may be
	 *            empty and/or immutable.
	 * @throws ConfigurationException
	 *             thrown when configuration failed seriously enough, so the
	 *             object cannot restore from this error and will not work
	 */
	void configure(Map<String, String> configuration)
			throws ConfigurationException;
}