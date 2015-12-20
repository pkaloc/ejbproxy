package ejbproxy.runtime.basic.checkers;

import ejbproxy.runtime.configuration.ConfigurationException;
import ejbproxy.runtime.endpoint.ConfigurableAbstractHealthChecker;
import ejbproxy.runtime.endpoint.Endpoint.Status;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import org.slf4j.Logger;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A health checker which checks a given endpoint´s health by calling the '
 * <code>String echo(String)</code>' method of its 'echo' service.
 * <p>
 * This class is configurable and expects configuration key with name
 * {@value #ECHO_SERVICE_JNDI_BINDING_KEY}. The value of this key is then used
 * as jndi binding of the 'echo' service.
 * <p>
 * Note: The {@link #configure(java.util.Map)} method must be called prior an
 * endpoint is set.
 */
public class EchoServiceHealthChecker extends ConfigurableAbstractHealthChecker {
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(EchoServiceHealthChecker.class);

	private static final String ECHO_SERVICE_JNDI_BINDING_KEY = "servicebinding";

	private static final String ECHO_MSG_TO_SEND = "Are you alive? I hope so!";

	private Object echoService;
	private Method echoMethod;

	@Override
	public void checkHealth() {
		if (echoService == null) {
			lookupEchoService();
		}

		if (echoMethod != null) {
			try {
				if (ECHO_MSG_TO_SEND.equals(echoMethod.invoke(echoService,
						ECHO_MSG_TO_SEND))) {
					if (endpoint.getStatus() != Status.OK) {
						endpoint.setStatus(Status.OK);
					}
				} else {
					if (endpoint.getStatus() != Status.ERROR) {
						endpoint.setStatus(Status.ERROR);
					}
				}
			} catch (IllegalArgumentException e) {
				log.warn("An unexpected error while calling 'echo' service! "
						+ getErrMsgSuffix(), e);
			} catch (IllegalAccessException e) {
				log.warn("An unexpected error while calling 'echo' service! "
						+ getErrMsgSuffix(), e);
			} catch (InvocationTargetException e) {
				log.info(
						"The 'echo' service heatlh check failed! Setting the endpoint´s status to ERROR. "
								+ getErrMsgSuffix(), e);
				if (endpoint.getStatus() != Status.ERROR) {
					endpoint.setStatus(Status.ERROR);
				}
			}
		} else {
			if (endpoint.getStatus() != Status.ERROR) {
				endpoint.setStatus(Status.ERROR);
			}
		}
	}

	// --- Configurable API ---

	@Override
	public void configure(Map<String, String> configuration) {
		if (!configuration.containsKey(ECHO_SERVICE_JNDI_BINDING_KEY)) {
			throw new ConfigurationException(
					"The passed configuration does not contain required '"
							+ ECHO_SERVICE_JNDI_BINDING_KEY + "' key!");
		}
		super.configure(configuration);
	}

	// --- Object API ---

	@Override
	public String toString() {
		return "EchoServiceHealthChecker [binding="
				+ configuration.get(ECHO_SERVICE_JNDI_BINDING_KEY)
				+ ", endpoint=" + endpoint.getURL() + "]";
	}

	// --- Utilities ---

	private synchronized void lookupEchoService() {
		try {
			echoService = endpoint.lookup(configuration
					.get(ECHO_SERVICE_JNDI_BINDING_KEY));
			echoMethod = echoService.getClass().getMethod("echo", String.class);
		} catch (NamingException ignored) {
			// lookup may work on next health check
		} catch (Exception e) {
			log.warn(
					"Corrupt 'echo' service binding! This health checking won´t work until new correct configuration/endpoint is set! "
							+ getErrMsgSuffix(), e);
		}
	}

	private String getErrMsgSuffix() {
		return String.format("Service binding: '%s', endpoint url: '%s'.",
				configuration.get(ECHO_SERVICE_JNDI_BINDING_KEY),
				endpoint.getURL());
	}

}
