package ejbproxy.runtime.configuration.initial;

import ejbproxy.runtime.configuration.RuntimeConfiguration;
import ejbproxy.runtime.configuration.initial.elements.ComposerElement;
import ejbproxy.runtime.configuration.initial.elements.EndpointElement;
import ejbproxy.runtime.configuration.initial.elements.HandlerElement;
import ejbproxy.runtime.configuration.initial.elements.LoggerElement;
import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.invocation.InvocationHandler;
import ejbproxy.runtime.invocation.InvocationLogger;
import ejbproxy.runtime.util.jndi.JndiGlobalNameComposer;

import java.util.Map;

/**
 * Represents the initial configuration of EJBProxy. This configuration is used
 * by other EJBProxy components at runtime. Typically by the
 * {@link RuntimeConfiguration} which loads its start-up configuration from it.
 * <p>
 * EJBProxy implementations must guarantee that at most one initial
 * configuration is created and so available to other components.
 * <p>
 * Technical note: Implementations should load the configuration eagerly inside
 * a constructor and log and throw the <code>EJBProxyRuntimeException</code>
 * when a serious error occurs.
 * 
 * @see RuntimeConfiguration
 * @see InitialConfigurationHolder
 */
public interface InitialConfiguration {
	/**
	 * The <code>RuntimeConfiguration</code> exposes parts of its API thru JMX.
	 * This API is required to expose under the name returned by this method.
	 */
	String getRuntimeConfigurationJMXObjectName();

	/**
	 * Retrieves the composer which will be composing jndi global names.
	 * 
	 * @see JndiGlobalNameComposer
	 */
	ComposerElement getComposer();

	/**
	 * Returns all defined endpoints.
	 * 
	 * @see Endpoint
	 */
	Iterable<EndpointElement> getAllEndpoints();

	/**
	 * Returns all defined handlers.
	 * 
	 * @see InvocationHandler
	 */
	Iterable<HandlerElement> getAllHandlers();

	/**
	 * Returns all defined loggers.
	 * 
	 * @see InvocationLogger
	 */
	Iterable<LoggerElement> getAllLoggers();

	/**
	 * Returns the default initial runtime configuration.
	 * <p>
	 * This configuration is valid for all such ejbs which were not explicitly
	 * configured (that is no custom configuration is available to them).
	 */
	Runtime getDefaultInitialRuntimeConfiguration();

	/**
	 * Returns initial runtime configurations of all explicitly configured ejbs.
	 * <p>
	 * That is, the map holds configurations for all such ejbs the default
	 * configuration is not valid to them.
	 */
	Map<String, Runtime> getInitialRuntimeConfigurations();

	/**
	 * Holds initial runtime configuration of a given ejb.
	 */
	interface Runtime {
		/**
		 * Marks whether EJBProxy is initially enabled for the given ejb.
		 */
		boolean isEJBProxyEnabled();

		/**
		 * Returns all endpoints which will be initially available to the given
		 * ejb.
		 * 
		 * @see Endpoint
		 */
		Iterable<EndpointElement> getEndpoints();

		/**
		 * Returns the handler which will be initially available to the given
		 * ejb.
		 * 
		 * @see InvocationHandler
		 */
		HandlerElement getHandler();

		/**
		 * Returns the logger which will be initially available to the given
		 * ejb.
		 * 
		 * @see InvocationLogger
		 */
		LoggerElement getLogger();

		/**
		 * Marks whether invocations logging is initially enabled for the given
		 * ejb.
		 * 
		 * @see InvocationLogger
		 */
		boolean isLoggingEnabled();

		/**
		 * Returns the name under which the <code>InvocationLogger</code> set
		 * for the passed ejb will log.
		 * 
		 * @see InvocationLogger
		 */
		String getLoggerName();
	}
}
