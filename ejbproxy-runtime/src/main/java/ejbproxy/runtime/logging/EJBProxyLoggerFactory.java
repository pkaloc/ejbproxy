package ejbproxy.runtime.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * EJBProxy uses (and all its runtime extensions should use) the Logback Project
 * as logging framework. But, the standard {@link LoggerFactory} is not used at
 * all, all {@link Logger}s are/should be instantiated by this factory instead.
 * <p>
 * The reason for this is simple, EJBProxy should not interfere with any
 * possible logging framework used by the enclosing EAR file. And custom
 * <code>LoggerFactory</code> is the easiest solution how to accomplish this,
 * since we cannot use the standard <code>ContextSelector</code> mechanism
 * because of usage of system properties.
 */
// TODO - develop a robust solution for complete logback isolation
// At the present time, we let EJBProxy defining classloader
// load the embedded logback impl and hoping we won´t do any harm while still
// having expected logback configuration for EJBProxy core and all its
// components
public class EJBProxyLoggerFactory {

	static {
		applyEJBProxyLoggingConfiguration();
	}

	public static Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}

	public static Logger getLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}

	private static void applyEJBProxyLoggingConfiguration() {
		InputStream loggingConfig = EJBProxyLoggerFactory.class
				.getResourceAsStream("logback.groovy");
		if (loggingConfig == null) {
			loggingConfig = EJBProxyLoggerFactory.class
					.getResourceAsStream("logback.xml");
		}

		if (loggingConfig != null) {
			LoggerContext context = (LoggerContext) LoggerFactory
					.getILoggerFactory();

			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				// Shall we reset curr. logback config here
				// context.reset();
				configurator.doConfigure(loggingConfig);
			} catch (JoranException je) {
				// StatusPrinter will handle this
			} finally {
				try {
					loggingConfig.close();
				} catch (IOException ignore) {
				}
			}
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}
	}
}
