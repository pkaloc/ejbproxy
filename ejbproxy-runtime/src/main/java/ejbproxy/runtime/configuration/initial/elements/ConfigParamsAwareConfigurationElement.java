package ejbproxy.runtime.configuration.initial.elements;

import java.util.Map;

public interface ConfigParamsAwareConfigurationElement extends
		ConfigurationElement {
	Map<String, String> getConfigParams();
}
