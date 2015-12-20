package ejbproxy.runtime.configuration;

import ejbproxy.runtime.EJBProxyRuntimeException;
import ejbproxy.runtime.configuration.initial.InitialConfiguration;
import ejbproxy.runtime.configuration.initial.InitialConfiguration.Runtime;
import ejbproxy.runtime.configuration.initial.InitialConfigurationHolder;
import ejbproxy.runtime.configuration.initial.elements.EndpointElement;
import ejbproxy.runtime.configuration.initial.elements.HandlerElement;
import ejbproxy.runtime.configuration.initial.elements.LoggerElement;
import ejbproxy.runtime.configuration.jmx.RuntimeConfigurationChangeNotification;
import ejbproxy.runtime.configuration.util.HealthCheckerThread;
import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.endpoint.HealthChecker;
import ejbproxy.runtime.invocation.InvocationHandler;
import ejbproxy.runtime.invocation.InvocationLogger;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import ejbproxy.runtime.util.jndi.JndiGlobalNameComposer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO - copy methods´ javadocs from old initial configuration. Mention no-arg
 * constructors, url endpoint constructor, configure method invocations If
 * configurationexception, EJBProxy will not work
 */
public class RuntimeConfiguration extends NotificationBroadcasterSupport
		implements RuntimeConfigurationMBean {
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(RuntimeConfiguration.class);

	static {
		// we want to trigger runtime configuration initialization immediately
		// at deployment time
		// this way, any possible error will be visible to the deployer early
		get();
	}

	private final InitialConfiguration initialConfig = InitialConfigurationHolder
			.getConfiguration();

	private JndiGlobalNameComposer composer;

	private final Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();
	private final Map<String, Class<? extends InvocationHandler>> handlers = new HashMap<String, Class<? extends InvocationHandler>>();
	private final Map<String, InvocationLogger> loggers = new HashMap<String, InvocationLogger>();

	private final ConcreteEJBRuntimeConfiguration defaultConfig = new ConcreteEJBRuntimeConfiguration();
	private final Map<String, ConcreteEJBRuntimeConfiguration> configs = new HashMap<String, RuntimeConfiguration.ConcreteEJBRuntimeConfiguration>();

	private ObjectName jmxName;
	private long notificationSequence = 0;

	private RuntimeConfiguration() {
		initializeComposer();
		initializeEndpoints();
		checkHandlers();
		initializeLoggers();

		setUpDefaultConfiguration();
		setUpConfigurations();

		registerItselfIntoPlatformMBeanServer();
	}

	// --- Convenient methods for other EJBProxy components

	public JndiGlobalNameComposer getComposerObject() {
		return composer;
	}

	public List<Endpoint> getEndpointObjects(Iterable<String> names) {
		Validate.noNullElements(names);

		List<Endpoint> result = new ArrayList<Endpoint>();
		for (String name : names) {
			result.add(endpoints.get(name));
		}
		return result;
	}

	public InvocationHandler newHandlerObject(String name) {
		Validate.notNull(name);

		try {
			InvocationHandler resultingHandler = handlers.get(name)
					.newInstance();
			if (resultingHandler instanceof Configurable) {
				for (HandlerElement handlerElem : initialConfig
						.getAllHandlers()) {
					if (handlerElem.getName().equals(name)) {
						((Configurable) resultingHandler).configure(handlerElem
								.getConfigParams());
						break;
					}
				}
			}
			return resultingHandler;
		} catch (Exception e) {
			// Should not happen exception occurred!
			// What shall we do here?!
			log.error("Unable to configure the '{}' handler!", name, e);
			return null;
		}
	}

	public InvocationLogger getLoggerObject(String name) {
		Validate.notNull(name);
		return loggers.get(name);
	}

	public ObjectName getJMXObjectName() {
		return jmxName;
	}

	// --- Singleton API ---

	public static RuntimeConfiguration get() {
		return RuntimeConfigurationHolder.instance;
	}

	// --- RuntimeConfigurationMBean API ---

	@Override
	public boolean isEJBProxyEnabled() {
		return defaultConfig.ejbProxyEnabled;
	}

	@Override
	public void setEJBProxyEnabled(boolean enabled, boolean resetExisting) {
		synchronized (defaultConfig) {
			if (resetExisting || defaultConfig.ejbProxyEnabled != enabled) {
				defaultConfig.ejbProxyEnabled = enabled;
				if (resetExisting) {
					for (ConcreteEJBRuntimeConfiguration config : configs
							.values()) {
						config.ejbProxyEnabled = null;
					}
				}

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						RuntimeConfigurationChangeNotification.ConfigurationElement.EJBPROXY_ENABLED,
						!enabled, enabled));
			}
		}
	}

	@Override
	public List<String> getDefaultEndpoints() {
		return Collections.unmodifiableList(defaultConfig.endpoints);
	}

	@Override
	public void setDefaultEndpoints(List<String> endpoints,
			boolean resetExisting) {
		Validate.noNullElements(endpoints);

		for (String endpointName : endpoints) {
			if (this.endpoints.get(endpointName) == null) {
				throw new EJBProxyRuntimeException(String.format(
						"Illegal endpoint specified: %s", endpointName));
			}
		}

		synchronized (defaultConfig) {
			if (resetExisting || !defaultConfig.endpoints.equals(endpoints)) {
				Object oldValue = defaultConfig.endpoints;

				defaultConfig.endpoints = endpoints;
				if (resetExisting) {
					for (ConcreteEJBRuntimeConfiguration config : configs
							.values()) {
						config.endpoints = null;
					}
				}

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						RuntimeConfigurationChangeNotification.ConfigurationElement.ENDPOINTS,
						oldValue, endpoints));
			}
		}
	}

	@Override
	public String getDefaultHandler() {
		return defaultConfig.handler;
	}

	@Override
	public void setDefaultHandler(String handler, boolean resetExisting) {
		Validate.notNull(handler);

		if (this.handlers.get(handler) == null) {
			throw new EJBProxyRuntimeException(String.format(
					"Illegal handler specified: %s", handler));
		}

		synchronized (defaultConfig) {
			if (resetExisting || !defaultConfig.handler.equals(handler)) {
				Object oldValue = defaultConfig.handler;

				defaultConfig.handler = handler;
				if (resetExisting) {
					for (ConcreteEJBRuntimeConfiguration config : configs
							.values()) {
						config.handler = null;
					}
				}

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						RuntimeConfigurationChangeNotification.ConfigurationElement.HANDLER,
						oldValue, handler));
			}
		}
	}

	@Override
	public boolean isLoggingEnabled() {
		return defaultConfig.loggingEnabled;
	}

	@Override
	public void setLoggingEnabled(boolean enabled, boolean resetExisting) {
		synchronized (defaultConfig) {
			if (resetExisting || defaultConfig.loggingEnabled != enabled) {
				defaultConfig.loggingEnabled = enabled;
				if (resetExisting) {
					for (ConcreteEJBRuntimeConfiguration config : configs
							.values()) {
						config.loggingEnabled = null;
					}
				}

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						RuntimeConfigurationChangeNotification.ConfigurationElement.LOGGING_ENABLED,
						!enabled, enabled));
			}
		}
	}

	@Override
	public String getDefaultLogger() {
		return defaultConfig.logger;
	}

	@Override
	public void setDefaultLogger(String logger, boolean resetExisting) {
		Validate.notNull(logger);

		if (this.loggers.get(logger) == null) {
			throw new EJBProxyRuntimeException(String.format(
					"Illegal logger specified: %s", logger));
		}

		synchronized (defaultConfig) {
			if (resetExisting || !defaultConfig.logger.equals(logger)) {
				Object oldValue = defaultConfig.logger;

				defaultConfig.logger = logger;
				if (resetExisting) {
					for (ConcreteEJBRuntimeConfiguration config : configs
							.values()) {
						config.logger = null;
					}
				}

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						RuntimeConfigurationChangeNotification.ConfigurationElement.LOGGER,
						oldValue, logger));
			}
		}
	}

	@Override
	public String getDefaultLoggerName() {
		return defaultConfig.loggerName;
	}

	@Override
	public void setDefaultLoggerName(String loggerName, boolean resetExisting) {
		Validate.notNull(loggerName);

		synchronized (defaultConfig) {
			if (resetExisting || !defaultConfig.loggerName.equals(loggerName)) {
				Object oldValue = defaultConfig.loggerName;

				defaultConfig.loggerName = loggerName;
				if (resetExisting) {
					for (ConcreteEJBRuntimeConfiguration config : configs
							.values()) {
						config.loggerName = null;
					}
				}

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						RuntimeConfigurationChangeNotification.ConfigurationElement.LOGGER_NAME,
						oldValue, loggerName));
			}
		}
	}

	@Override
	public Boolean isEJBProxyEnabled(String jndiBinding) {
		Validate.notNull(jndiBinding);

		if (configs.containsKey(jndiBinding)) {
			return configs.get(jndiBinding).ejbProxyEnabled;
		} else {
			return null;
		}
	}

	@Override
	public void setEJBProxyEnabled(String jndiBinding, boolean enabled) {
		Validate.notNull(jndiBinding);

		synchronized (configs) {
			ConcreteEJBRuntimeConfiguration ejbConfig;
			if (configs.containsKey(jndiBinding)) {
				ejbConfig = configs.get(jndiBinding);
			} else {
				ejbConfig = new ConcreteEJBRuntimeConfiguration();
				configs.put(jndiBinding, ejbConfig);
			}

			if (!ObjectUtils.equals(ejbConfig.ejbProxyEnabled, enabled)) {
				ejbConfig.ejbProxyEnabled = enabled;

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						jndiBinding,
						RuntimeConfigurationChangeNotification.ConfigurationElement.EJBPROXY_ENABLED,
						!enabled, enabled));
			}
		}
	}

	@Override
	public List<String> getEndpoints(String jndiBinding) {
		Validate.notNull(jndiBinding);

		if (configs.containsKey(jndiBinding)) {
			return configs.get(jndiBinding).endpoints;
		} else {
			return null;
		}
	}

	@Override
	public void setEndpoints(String jndiBinding, List<String> endpoints) {
		Validate.notNull(jndiBinding);
		Validate.noNullElements(endpoints);

		for (String endpointName : endpoints) {
			if (this.endpoints.get(endpointName) == null) {
				throw new EJBProxyRuntimeException(String.format(
						"Illegal endpoint specified: %s", endpointName));
			}
		}

		synchronized (configs) {
			ConcreteEJBRuntimeConfiguration ejbConfig;

			if (configs.containsKey(jndiBinding)) {
				ejbConfig = configs.get(jndiBinding);
			} else {
				ejbConfig = new ConcreteEJBRuntimeConfiguration();
				configs.put(jndiBinding, ejbConfig);
			}

			if (!ObjectUtils.equals(ejbConfig.endpoints, endpoints)) {
				Object oldValue = ejbConfig.endpoints;
				ejbConfig.endpoints = endpoints;

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						jndiBinding,
						RuntimeConfigurationChangeNotification.ConfigurationElement.ENDPOINTS,
						oldValue, endpoints));
			}
		}
	}

	@Override
	public String getHandler(String jndiBinding) {
		Validate.notNull(jndiBinding);

		if (configs.containsKey(jndiBinding)) {
			return configs.get(jndiBinding).handler;
		} else {
			return null;
		}
	}

	@Override
	public void setHandler(String jndiBinding, String handler) {
		Validate.notNull(jndiBinding);
		Validate.notNull(handler);

		if (this.handlers.get(handler) == null) {
			throw new EJBProxyRuntimeException(String.format(
					"Illegal handler specified: %s", handler));
		}

		synchronized (configs) {
			ConcreteEJBRuntimeConfiguration ejbConfig;

			if (configs.containsKey(jndiBinding)) {
				ejbConfig = configs.get(jndiBinding);
			} else {
				ejbConfig = new ConcreteEJBRuntimeConfiguration();
				configs.put(jndiBinding, ejbConfig);
			}

			if (!ObjectUtils.equals(ejbConfig.handler, handler)) {
				Object oldValue = ejbConfig.handler;
				ejbConfig.handler = handler;

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						jndiBinding,
						RuntimeConfigurationChangeNotification.ConfigurationElement.HANDLER,
						oldValue, handler));
			}
		}
	}

	@Override
	public Boolean isLoggingEnabled(String jndiBinding) {
		Validate.notNull(jndiBinding);

		if (configs.containsKey(jndiBinding)) {
			return configs.get(jndiBinding).loggingEnabled;
		} else {
			return null;
		}
	}

	@Override
	public void setLoggingEnabled(String jndiBinding, boolean enabled) {
		Validate.notNull(jndiBinding);

		synchronized (configs) {
			ConcreteEJBRuntimeConfiguration ejbConfig;

			if (configs.containsKey(jndiBinding)) {
				ejbConfig = configs.get(jndiBinding);
			} else {
				ejbConfig = new ConcreteEJBRuntimeConfiguration();
				configs.put(jndiBinding, ejbConfig);
			}

			if (!ObjectUtils.equals(ejbConfig.loggingEnabled, enabled)) {
				ejbConfig.loggingEnabled = enabled;

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						jndiBinding,
						RuntimeConfigurationChangeNotification.ConfigurationElement.LOGGING_ENABLED,
						!enabled, enabled));
			}
		}
	}

	@Override
	public String getLogger(String jndiBinding) {
		Validate.notNull(jndiBinding);

		if (configs.containsKey(jndiBinding)) {
			return configs.get(jndiBinding).logger;
		} else {
			return null;
		}
	}

	@Override
	public void setLogger(String jndiBinding, String logger) {
		Validate.notNull(jndiBinding);
		Validate.notNull(logger);

		if (this.loggers.get(logger) == null) {
			throw new EJBProxyRuntimeException(String.format(
					"Illegal logger specified: %s", logger));
		}

		synchronized (configs) {
			ConcreteEJBRuntimeConfiguration ejbConfig;

			if (configs.containsKey(jndiBinding)) {
				ejbConfig = configs.get(jndiBinding);
			} else {
				ejbConfig = new ConcreteEJBRuntimeConfiguration();
				configs.put(jndiBinding, ejbConfig);
			}

			if (!ObjectUtils.equals(ejbConfig.logger, logger)) {
				Object oldValue = ejbConfig.logger;
				ejbConfig.logger = logger;

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						jndiBinding,
						RuntimeConfigurationChangeNotification.ConfigurationElement.LOGGER,
						oldValue, logger));
			}
		}
	}

	@Override
	public String getLoggerName(String jndiBinding) {
		Validate.notNull(jndiBinding);

		if (configs.containsKey(jndiBinding)) {
			return configs.get(jndiBinding).loggerName;
		} else {
			return null;
		}
	}

	@Override
	public void setLoggerName(String jndiBinding, String loggerName) {
		Validate.notNull(jndiBinding);
		Validate.notNull(loggerName);

		synchronized (configs) {
			ConcreteEJBRuntimeConfiguration ejbConfig;

			if (configs.containsKey(jndiBinding)) {
				ejbConfig = configs.get(jndiBinding);
			} else {
				ejbConfig = new ConcreteEJBRuntimeConfiguration();
				configs.put(jndiBinding, ejbConfig);
			}

			if (!ObjectUtils.equals(ejbConfig.loggerName, loggerName)) {
				Object oldValue = ejbConfig.loggerName;
				ejbConfig.loggerName = loggerName;

				sendNotification(new RuntimeConfigurationChangeNotification(
						RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION,
						this,
						++notificationSequence,
						jndiBinding,
						RuntimeConfigurationChangeNotification.ConfigurationElement.LOGGER_NAME,
						oldValue, loggerName));
			}
		}
	}

	// --- Utilities ---

	private void initializeComposer() {
		try {
			composer = (JndiGlobalNameComposer) Class.forName(
					initialConfig.getComposer().getClassName()).newInstance();
			if (composer instanceof Configurable) {
				((Configurable) composer).configure(initialConfig.getComposer()
						.getConfigParams());
			}
		} catch (Exception e) {
			log.error("Unable to configure the composer!", e);
			throw (e instanceof ConfigurationException) ? (ConfigurationException) e
					: new ConfigurationException(e);
		}
	}

	private void initializeEndpoints() {
		for (EndpointElement endpointElem : initialConfig.getAllEndpoints()) {
			try {
				Endpoint endpoint = (Endpoint) Class
						.forName(endpointElem.getClassName())
						.getConstructor(String.class)
						.newInstance(endpointElem.getURL());
				if (endpoint instanceof Configurable) {
					((Configurable) endpoint).configure(endpointElem
							.getConfigParams());
				}

				if (endpointElem.getHealthCheckerElement() != null) {
					initializeHealthChecker(endpointElem, endpoint);
				}
				endpoints.put(endpointElem.getName(), endpoint);
			} catch (Exception e) {
				log.error("Unable to configure the '{}' endpoint!",
						endpointElem.getName(), e);
				throw (e instanceof ConfigurationException) ? (ConfigurationException) e
						: new ConfigurationException(e);
			}
		}
	}

	private void initializeHealthChecker(EndpointElement endpointElement,
			Endpoint endpoint) {
		try {
			HealthChecker checker = (HealthChecker) Class.forName(
					endpointElement.getHealthCheckerElement().getClassName())
					.newInstance();
			if (checker instanceof Configurable) {
				((Configurable) checker).configure(endpointElement
						.getHealthCheckerElement().getConfigParams());
			}

			checker.setEndpoint(endpoint);

			new HealthCheckerThread(endpointElement, checker).start();
		} catch (Exception e) {
			log.error("Unable to configure the '{}' health checker!",
					endpointElement.getHealthCheckerElement().getName(), e);
			throw (e instanceof ConfigurationException) ? (ConfigurationException) e
					: new ConfigurationException(e);
		}
	}

	private void checkHandlers() {
		for (HandlerElement handlerElem : initialConfig.getAllHandlers()) {
			try {
				InvocationHandler handler = (InvocationHandler) Class.forName(
						handlerElem.getClassName()).newInstance();
				if (handler instanceof Configurable) {
					((Configurable) handler).configure(handlerElem
							.getConfigParams());
				}

				handlers.put(handlerElem.getName(), handler.getClass());
			} catch (Exception e) {
				log.error("Unable to configure the '{}' handler!",
						handlerElem.getName(), e);
				throw (e instanceof ConfigurationException) ? (ConfigurationException) e
						: new ConfigurationException(e);
			}
		}
	}

	private void initializeLoggers() {
		for (LoggerElement loggerElem : initialConfig.getAllLoggers()) {
			try {
				InvocationLogger logger = (InvocationLogger) Class.forName(
						loggerElem.getClassName()).newInstance();
				if (logger instanceof Configurable) {
					((Configurable) logger).configure(loggerElem
							.getConfigParams());
				}

				loggers.put(loggerElem.getName(), logger);
			} catch (Exception e) {
				log.error("Unable to configure the '{}' logger!",
						loggerElem.getName(), e);
				throw (e instanceof ConfigurationException) ? (ConfigurationException) e
						: new ConfigurationException(e);
			}
		}
	}

	private void setUpDefaultConfiguration() {
		Runtime defInitConfig = InitialConfigurationHolder.getConfiguration()
				.getDefaultInitialRuntimeConfiguration();

		defaultConfig.ejbProxyEnabled = defInitConfig.isEJBProxyEnabled();
		defaultConfig.handler = defInitConfig.getHandler().getName();
		defaultConfig.loggingEnabled = defInitConfig.isLoggingEnabled();
		defaultConfig.logger = defInitConfig.getLogger().getName();
		defaultConfig.loggerName = defInitConfig.getLoggerName();

		List<String> definedEndpoints = new ArrayList<String>();
		for (EndpointElement endpointElem : defInitConfig.getEndpoints()) {
			definedEndpoints.add(endpointElem.getName());
		}
		defaultConfig.endpoints = definedEndpoints;
	}

	private void setUpConfigurations() {
		Map<String, Runtime> initConfigs = InitialConfigurationHolder
				.getConfiguration().getInitialRuntimeConfigurations();

		for (String ejb : initConfigs.keySet()) {
			Runtime initConfig = initConfigs.get(ejb);
			ConcreteEJBRuntimeConfiguration config = new ConcreteEJBRuntimeConfiguration();

			config.ejbProxyEnabled = initConfig.isEJBProxyEnabled();
			config.handler = initConfig.getHandler().getName();
			config.loggingEnabled = initConfig.isLoggingEnabled();
			config.logger = initConfig.getLogger().getName();
			config.loggerName = initConfig.getLoggerName();

			List<String> definedEndpoints = new ArrayList<String>();
			for (EndpointElement endpointElem : initConfig.getEndpoints()) {
				definedEndpoints.add(endpointElem.getName());
			}
			config.endpoints = definedEndpoints;

			configs.put(ejb, config);
		}
	}

	private void registerItselfIntoPlatformMBeanServer() {
		String jmxNameAsString = InitialConfigurationHolder.getConfiguration()
				.getRuntimeConfigurationJMXObjectName();
		try {
			jmxName = new ObjectName(jmxNameAsString);
		} catch (Exception e) {
			log.warn(
					"Illegal RuntimeConfigurationMBean´s name '{}' specified. The RuntimeConfigurationMBean will not be registered into the platform mbean server!",
					jmxNameAsString);
			return;
		}

		try {
			ManagementFactory.getPlatformMBeanServer().registerMBean(this,
					jmxName);
		} catch (InstanceAlreadyExistsException e) {
			try {
				if (!ManagementFactory.getPlatformMBeanServer().isInstanceOf(
						jmxName, RuntimeConfigurationMBean.class.getName())) {
					log.warn(
							"The RuntimeConfigurationMBean could not be registered into the platform mbean server. It is already occupied by a different type. It seems EJBProxy was loaded more than once. Please note that JMX configuration may not work correctly for some ejbs!",
							e);
				}
			} catch (InstanceNotFoundException iNotFoundEx) {
				log.warn(
						"The RuntimeConfigurationMBean could not be registered into the platform mbean server!",
						e);
			}
		} catch (Exception e) {
			log.warn(
					"The RuntimeConfigurationMBean could not be registered into the platform mbean server! JMX configuration will not work.",
					e);
		}
	}

	// --- Utility types ---

	private static class RuntimeConfigurationHolder {
		public static final RuntimeConfiguration instance = new RuntimeConfiguration();
	}

	private static class ConcreteEJBRuntimeConfiguration {
		private Boolean ejbProxyEnabled;
		private List<String> endpoints;
		private String handler;
		private Boolean loggingEnabled;
		private String logger;
		private String loggerName;
	}
}
