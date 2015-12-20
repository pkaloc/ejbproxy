package ejbproxy.runtime.configuration.initial.elements;

import java.util.Map;

public class ImmutableComposerElement implements ComposerElement {
	private final ConfigurationElementSupport support;

	public ImmutableComposerElement(String className,
			Map<String, String> configParams) {
		support = new ConfigurationElementSupport();
		support.setClassName(className);
		support.setConfigParams(configParams);
	}

	@Override
	public Map<String, String> getConfigParams() {
		return support.getConfigParams();
	}

	@Override
	public String getClassName() {
		return support.getClassName();
	}

}
