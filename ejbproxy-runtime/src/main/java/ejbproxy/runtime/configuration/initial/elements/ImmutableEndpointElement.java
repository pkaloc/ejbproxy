package ejbproxy.runtime.configuration.initial.elements;

import org.apache.commons.lang3.Validate;

import java.util.Map;

public class ImmutableEndpointElement implements EndpointElement {
	private final ConfigurationElementSupport support;
	private final String url;
	private final HealthCheckerElement healthChecker;

	public ImmutableEndpointElement(String name, String className,
			Map<String, String> configParams, String url,
			ImmutableHealthCheckerElement healthChecker) {
		support = new ConfigurationElementSupport();
		support.setName(name);
		support.setClassName(className);
		support.setConfigParams(configParams);

		this.url = Validate.notBlank(url);
		this.healthChecker = healthChecker;
	}

	@Override
	public String getName() {
		return support.getName();
	}

	@Override
	public String getClassName() {
		return support.getClassName();
	}

	@Override
	public Map<String, String> getConfigParams() {
		return support.getConfigParams();
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public HealthCheckerElement getHealthCheckerElement() {
		return healthChecker;
	}
}
