package ejbproxy.runtime.configuration.initial.elements;

public interface NamedConfigurationElement extends ConfigurationElement {
	/**
	 * The name must not be <code>null</code>, empty or blank.
	 */
	String getName();
}
