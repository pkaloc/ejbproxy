package ejbproxy.runtime.endpoint;

import ejbproxy.runtime.configuration.Configurable;

import java.util.Map;

/**
 * Configurable abstract endpoint.
 */
public abstract class ConfigurableAbstractEndpoint extends AbstractEndpoint
		implements Configurable {
	protected Map<String, String> configuration;

	public ConfigurableAbstractEndpoint(String url) {
		super(url);
	}

	@Override
	public void configure(Map<String, String> configuration) {
		this.configuration = configuration;
	}
}