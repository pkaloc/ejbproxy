package ejbproxy.runtime.configuration.initial.elements;

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


class ConfigurationElementSupport implements NamedConfigurationElement,
		ConfigParamsAwareConfigurationElement, InstantiableConfigurationElement {
	private String name;
	private Map<String, String> configParams;
	private String className;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = Validate.notBlank(name);
	}

	@Override
	public Map<String, String> getConfigParams() {
		return configParams != null ? Collections.unmodifiableMap(configParams)
				: null;
	}

	public void setConfigParams(Map<String, String> configParams) {
		Validate.noNullElements(configParams.keySet());
		Validate.noNullElements(configParams.values());
		this.configParams = new HashMap<String, String>(configParams);
	}

	@Override
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = Validate.notBlank(className);
	}
}
