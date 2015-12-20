package ejbproxy.runtime.endpoint;

import ejbproxy.runtime.configuration.Configurable;

import java.util.Map;

/**
 * Configurable endpoint health checker.
 */
public abstract class ConfigurableAbstractHealthChecker extends
		AbstractHealthChecker implements Configurable {
	protected Map<String, String> configuration;

	@Override
	public void configure(Map<String, String> configuration) {
		this.configuration = configuration;
	}
}