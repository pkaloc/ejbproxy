package ejbproxy.runtime.configuration.initial.elements;

import java.util.Map;

public class ImmutableLoggerElement implements LoggerElement {
	private final ConfigurationElementSupport support;

	public ImmutableLoggerElement(String name, String className,
			Map<String, String> configParams) {
		support = new ConfigurationElementSupport();
		support.setName(name);
		support.setClassName(className);
		support.setConfigParams(configParams);
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

}
