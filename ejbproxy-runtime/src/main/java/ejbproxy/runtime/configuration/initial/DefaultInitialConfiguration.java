package ejbproxy.runtime.configuration.initial;

import ejbproxy.runtime.EJBProxyRuntimeException;
import ejbproxy.runtime.configuration.initial.elements.ComposerElement;
import ejbproxy.runtime.configuration.initial.elements.EndpointElement;
import ejbproxy.runtime.configuration.initial.elements.HandlerElement;
import ejbproxy.runtime.configuration.initial.elements.ImmutableComposerElement;
import ejbproxy.runtime.configuration.initial.elements.ImmutableEndpointElement;
import ejbproxy.runtime.configuration.initial.elements.ImmutableHandlerElement;
import ejbproxy.runtime.configuration.initial.elements.ImmutableHealthCheckerElement;
import ejbproxy.runtime.configuration.initial.elements.ImmutableLoggerElement;
import ejbproxy.runtime.configuration.initial.elements.LoggerElement;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is the default <code>InitialConfiguration</code> implementation.
 * <p>
 * The configuration is loaded from the
 * <code>ejbproxy-initconfig.properties</code> file located inside the
 * <code>DefaultInitialConfiguration</code> class´s folder.
 * <p>
 * See the <code>ejbproxy-initconfig-sample.properties</code> file for more
 * info.
 */
public class DefaultInitialConfiguration implements InitialConfiguration {
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(DefaultInitialConfiguration.class);

	private static final OrderedProperties props = loadPropertiesFile("ejbproxy-initconfig.properties");
	private static final Set<String> propsKeys = props
			.orderedStringPropertyNames();

	private static final String runtimeConfigJMXObjectName = getRuntimeConfigJMXObjectName();

	private static final ComposerElement composer = createComposerElement();
	private static final Map<String, HandlerElement> handlers = createHandlerElements();
	private static final Map<String, LoggerElement> loggers = createLoggerElements();
	private static final Map<String, EndpointElement> endpoints = createEndpointElements();

	private static final boolean ejbProxyEnabled = getIsEJBProxyEnabled();

	private static final InitialRuntimeConfiguration defInitRuntimeConfig = createDefaultInitialRuntimeConfiguration();
	private static final Map<String, ? extends Runtime> initRuntimeConfigs = createInitialRuntimeConfigurations();

	// --- InitialConfiguration API ---

	@Override
	public String getRuntimeConfigurationJMXObjectName() {
		return runtimeConfigJMXObjectName;
	}

	@Override
	public ComposerElement getComposer() {
		return composer;
	}

	@Override
	public Iterable<EndpointElement> getAllEndpoints() {
		return Collections.unmodifiableCollection(endpoints.values());
	}

	@Override
	public Iterable<HandlerElement> getAllHandlers() {
		return Collections.unmodifiableCollection(handlers.values());
	}

	@Override
	public Iterable<LoggerElement> getAllLoggers() {
		return Collections.unmodifiableCollection(loggers.values());
	}

	@Override
	public Runtime getDefaultInitialRuntimeConfiguration() {
		return defInitRuntimeConfig;
	}

	@Override
	public Map<String, Runtime> getInitialRuntimeConfigurations() {
		return Collections.unmodifiableMap(initRuntimeConfigs);
	}

	// --- Utilities ---

	private static OrderedProperties loadPropertiesFile(String configFileName) {
		try {
			OrderedProperties props = new OrderedProperties();
			props.load(DefaultInitialConfiguration.class
					.getResourceAsStream(configFileName));
			return props;
		} catch (Exception e) {
			log.error(String.format("Unable to load the '%s' file!",
					configFileName), e);
			throw new EJBProxyRuntimeException(e);
		}
	}

	private static boolean isValidElementDefinition(String propKey,
			String elemType) {
		String elemTypePlusDot = elemType + ".";
		return propKey.startsWith(elemTypePlusDot)
				&& propKey.length() > elemTypePlusDot.length()
				&& propKey.lastIndexOf('.') == elemTypePlusDot.length() - 1;
	}

	private static Map<String, String> loadConfigParams(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		for (String propKey : propsKeys) {
			if (propKey.startsWith(prefix)) {
				params.put(propKey.substring(prefix.length()),
						props.getProperty(propKey));
			}
		}
		return params;
	}

	private static String getRuntimeConfigJMXObjectName() {
		if (props.containsKey("jmx.runtimeconfig.objectname")) {
			return props.getProperty("jmx.runtimeconfig.objectname");
		} else {
			return "ejbproxy.runtime.jmx:type=RuntimeConfiguration";
		}
	}

	private static ComposerElement createComposerElement() {
		if (props.containsKey("composer")) {
			return new ImmutableComposerElement(props.getProperty("composer"),
					loadConfigParams("composer."));
		} else {
			log.error("No composer was defined!");
			throw new EJBProxyRuntimeException("No composer was defined!");
		}
	}

	private static Map<String, HandlerElement> createHandlerElements() {
		Map<String, HandlerElement> foundHandlers = new LinkedHashMap<String, HandlerElement>();
		for (String propKey : propsKeys) {
			if (isValidElementDefinition(propKey, "handler")) {
				HandlerElement foundHandler = createHandlerElement(propKey
						.substring("handler.".length()));
				foundHandlers.put(foundHandler.getName(), foundHandler);
			}
		}

		if (!foundHandlers.isEmpty()) {
			return foundHandlers;
		} else {
			log.error("No handler was defined!");
			throw new EJBProxyRuntimeException("No handler was defined!");
		}
	}

	private static HandlerElement createHandlerElement(String name) {
		Map<String, String> configParams = loadConfigParams("handler.*.");
		configParams.putAll(loadConfigParams("handler." + name + "."));
		return new ImmutableHandlerElement(name, props.getProperty("handler."
				+ name), configParams);
	}

	private static Map<String, LoggerElement> createLoggerElements() {
		Map<String, LoggerElement> foundLoggers = new LinkedHashMap<String, LoggerElement>();
		for (String propKey : propsKeys) {
			if (isValidElementDefinition(propKey, "logger")) {
				LoggerElement foundLogger = createLoggerElement(propKey
						.substring("logger.".length()));
				foundLoggers.put(foundLogger.getName(), foundLogger);
			}
		}

		if (!foundLoggers.isEmpty()) {
			return foundLoggers;
		} else {
			log.error("No logger was defined!");
			throw new EJBProxyRuntimeException("No logger was defined!");
		}
	}

	private static LoggerElement createLoggerElement(String name) {
		Map<String, String> configParams = loadConfigParams("logger.*.");
		configParams.putAll(loadConfigParams("logger." + name + "."));
		return new ImmutableLoggerElement(name, props.getProperty("logger."
				+ name), configParams);
	}

	private static ImmutableHealthCheckerElement createCheckerElement(
			String name) {
		if (props.containsKey("checker." + name)) {
			Map<String, String> configParams = loadConfigParams("checker.*.");
			configParams.putAll(loadConfigParams("checker." + name + "."));
			return new ImmutableHealthCheckerElement(name,
					props.getProperty("checker." + name), configParams);
		} else {
			log.warn("No checker with name '{}' was defined!", name);
			return null;
		}
	}

	private static Map<String, EndpointElement> createEndpointElements() {
		Map<String, EndpointElement> foundEndpoints = new HashMap<String, EndpointElement>();
		for (String propKey : propsKeys) {
			if (isValidElementDefinition(propKey, "endpoint")) {
				EndpointElement foundEndpoint = createEndpointElement(propKey
						.substring("endpoint.".length()));
				if (foundEndpoint != null) {
					foundEndpoints.put(foundEndpoint.getName(), foundEndpoint);
				}
			}
		}

		if (!foundEndpoints.isEmpty()) {
			return foundEndpoints;
		} else {
			log.error("No endpoint was defined!");
			throw new EJBProxyRuntimeException("No endpoint was defined!");
		}
	}

	private static EndpointElement createEndpointElement(String name) {
		if (props.containsKey("endpoint." + name + ".url")) {
			Map<String, String> configParams = loadConfigParams("endpoint.*.");
			configParams.putAll(loadConfigParams("endpoint." + name + "."));
			configParams.remove("url");

			String checkerName = configParams.get("checker");
			configParams.remove("checker");

			return new ImmutableEndpointElement(name,
					props.getProperty("endpoint." + name), configParams,
					props.getProperty("endpoint." + name + ".url"),
					checkerName != null ? createCheckerElement(checkerName)
							: null);
		} else {
			log.warn(
					"No url specified for the '{}' endpoint! The endpoint will be skipped.",
					name);
			return null;
		}
	}

	private static boolean getIsEJBProxyEnabled() {
		String ejbproxyEnabledGlobally = props.getProperty("runtime.ejbproxyenabled.*");
		return ejbproxyEnabledGlobally == null || BooleanUtils.toBoolean(ejbproxyEnabledGlobally);
	}

	private static InitialRuntimeConfiguration createDefaultInitialRuntimeConfiguration() {
		InitialRuntimeConfiguration defConfig = new InitialRuntimeConfiguration();
		defConfig.ejbProxyEnabled = ejbProxyEnabled;
		defConfig.endpoints = propsKeys.contains("runtime.endpoint.*") ? getReferencedEndpoints("runtime.endpoint.*")
				: endpoints.values();
		defConfig.handler = propsKeys.contains("runtime.handler.*") ? getReferencedHandler("runtime.handler.*")
				: handlers.values().iterator().next();
		defConfig.logger = propsKeys.contains("runtime.logger.*") ? getReferencedLogger("runtime.logger.*")
				: loggers.values().iterator().next();
		defConfig.loggingEnabled = propsKeys
				.contains("runtime.loggingenabled.*") ? BooleanUtils
				.toBoolean(props.getProperty("runtime.loggingenabled.*"))
				: true;
		defConfig.loggerName = propsKeys.contains("runtime.loggername.*") ? props
				.getProperty("runtime.loggername.*")
				: "ejbproxy.runtime.invocation.InvocationLogger";
		return defConfig;
	}

	private static Map<String, ? extends Runtime> createInitialRuntimeConfigurations() {
		Map<String, InitialRuntimeConfiguration> configs = new HashMap<String, InitialRuntimeConfiguration>();

		String elemName;
		for (String propKey : propsKeys) {
			// runtime.ejbproxyenabled.[jndi-binding]
			if (propKey.startsWith("runtime.ejbproxyenabled.")
					&& !propKey.equals("runtime.ejbproxyenabled.*")) {
				elemName = propKey.substring("runtime.ejbproxyenabled."
						.length());
				if (!configs.containsKey(elemName)) {
					configs.put(elemName, new InitialRuntimeConfiguration());
				}
				configs.get(elemName).ejbProxyEnabled = BooleanUtils
						.toBoolean(props.getProperty(propKey));
			}
			// runtime.endpoint.[jndi-binding]
			else if (propKey.startsWith("runtime.endpoint.")
					&& !propKey.equals("runtime.endpoint.*")) {
				elemName = propKey.substring("runtime.endpoint.".length());
				if (!configs.containsKey(elemName)) {
					configs.put(elemName, new InitialRuntimeConfiguration());
				}
				configs.get(elemName).endpoints = getReferencedEndpoints(propKey);
			}
			// runtime.handler.[jndi-binding]
			else if (propKey.startsWith("runtime.handler.")
					&& !propKey.equals("runtime.handler.*")) {
				elemName = propKey.substring("runtime.handler.".length());
				if (!configs.containsKey(elemName)) {
					configs.put(elemName, new InitialRuntimeConfiguration());
				}
				configs.get(elemName).handler = getReferencedHandler(propKey);
			}
			// runtime.logger.[jndi-binding]
			else if (propKey.startsWith("runtime.logger.")
					&& !propKey.equals("runtime.logger.*")) {
				elemName = propKey.substring("runtime.logger.".length());
				if (!configs.containsKey(elemName)) {
					configs.put(elemName, new InitialRuntimeConfiguration());
				}
				configs.get(elemName).logger = getReferencedLogger(propKey);
			}
			// runtime.loggingenabled.[jndi-binding]
			else if (propKey.startsWith("runtime.loggingenabled.")
					&& !propKey.equals("runtime.loggingenabled.*")) {
				elemName = propKey
						.substring("runtime.loggingenabled.".length());
				if (!configs.containsKey(elemName)) {
					configs.put(elemName, new InitialRuntimeConfiguration());
				}
				configs.get(elemName).loggingEnabled = BooleanUtils
						.toBoolean(props.getProperty(propKey));
			}
			// runtime.loggername.[jndi-binding]
			else if (propKey.startsWith("runtime.loggername.")
					&& !propKey.equals("runtime.loggername.*")) {
				elemName = propKey.substring("runtime.loggername.".length());
				if (!configs.containsKey(elemName)) {
					configs.put(elemName, new InitialRuntimeConfiguration());
				}
				configs.get(elemName).loggerName = props.getProperty(propKey);
			}
		}

		return configs;
	}

	private static Iterable<EndpointElement> getReferencedEndpoints(
			String propKey) {
		Set<EndpointElement> effectiveEndpoints = new HashSet<EndpointElement>();

		for (String endpointName : props.getProperty(propKey).split(
				"\\s*(,|\\s)\\s*")) {
			if (endpoints.containsKey(endpointName)) {
				effectiveEndpoints.add(endpoints.get(endpointName));
			} else {
				log.warn(
						"No endpoint is specified under name '{}'! This endpoint assignement will be skipped.",
						endpointName);
			}
		}

		if (effectiveEndpoints.isEmpty()) {
			log.error("No effective endpoint(s) specified for '{}' property!",
					propKey);
			throw new EJBProxyRuntimeException(String.format(
					"No effective endpoint(s) specified for '%s' property!",
					propKey));
		}

		return effectiveEndpoints;
	}

	private static HandlerElement getReferencedHandler(String propKey) {
		String handlerName = props.getProperty(propKey);
		if (handlers.containsKey(handlerName)) {
			return handlers.get(handlerName);
		} else {
			log.error("Wrong handler name specified for '{}' property!",
					propKey);
			throw new EJBProxyRuntimeException(String.format(
					"Wrong handler name specified for '%s' property!", propKey));
		}
	}

	private static LoggerElement getReferencedLogger(String propKey) {
		String loggerName = props.getProperty(propKey);
		if (loggers.containsKey(loggerName)) {
			return loggers.get(loggerName);
		} else {
			log.error("Wrong logger name specified for '{}' property!", propKey);
			throw new EJBProxyRuntimeException(String.format(
					"Wrong logger name specified for '%s' property!", propKey));
		}
	}

	// --- Utility types ---

	private static class InitialRuntimeConfiguration implements Runtime {
		private Boolean ejbProxyEnabled;
		private Iterable<EndpointElement> endpoints;
		private HandlerElement handler;
		private LoggerElement logger;
		private Boolean loggingEnabled;
		private String loggerName;

		@Override
		public boolean isEJBProxyEnabled() {
			return ejbProxyEnabled != null ? ejbProxyEnabled
					: defInitRuntimeConfig.ejbProxyEnabled;
		}

		@Override
		public Iterable<EndpointElement> getEndpoints() {
			return endpoints != null ? endpoints
					: defInitRuntimeConfig.endpoints;
		}

		@Override
		public HandlerElement getHandler() {
			return handler != null ? handler : defInitRuntimeConfig.handler;
		}

		@Override
		public LoggerElement getLogger() {
			return logger != null ? logger : defInitRuntimeConfig.logger;
		}

		@Override
		public boolean isLoggingEnabled() {
			return loggingEnabled != null ? loggingEnabled
					: defInitRuntimeConfig.loggingEnabled;
		}

		@Override
		public String getLoggerName() {
			return loggerName != null ? loggerName
					: defInitRuntimeConfig.loggerName;
		}
	}

	private static class OrderedProperties extends Properties {
		private static final long serialVersionUID = 1L;

		private final Map<Object, Object> entries = new LinkedHashMap<Object, Object>();

		public Set<String> orderedStringPropertyNames() {
			Set<String> stringNames = new LinkedHashSet<String>();
			for (Map.Entry<Object, Object> entry : entries.entrySet()) {
				if (entry.getKey() instanceof String
						&& entry.getValue() instanceof String) {
					stringNames.add((String) entry.getKey());
				}
			}
			return stringNames;
		}

		// --- Properties API ---

		@Override
		public synchronized Object put(Object key, Object value) {
			// The 'put' method is internally used when properties are loaded
			// from a stream.
			entries.put(key, value);
			return super.put(key, value);
		}
	}
}
