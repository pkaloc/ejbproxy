package ejbproxy.runtime.invocation;

import ejbproxy.runtime.configuration.Configurable;

import java.util.Map;

public abstract class ConfigurableAbstractInvocationHandler extends
		AbstractInvocationHandler implements Configurable {
	protected Map<String, String> configuration;

	@Override
	public void configure(Map<String, String> configuration) {
		this.configuration = configuration;
	}
}
