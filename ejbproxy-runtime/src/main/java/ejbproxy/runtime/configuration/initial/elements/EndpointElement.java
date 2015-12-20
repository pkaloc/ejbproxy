package ejbproxy.runtime.configuration.initial.elements;

public interface EndpointElement extends NamedConfigurationElement,
		ConfigParamsAwareConfigurationElement, InstantiableConfigurationElement {
	String getURL();

	/**
	 * May be null.
	 */
	HealthCheckerElement getHealthCheckerElement();
}
