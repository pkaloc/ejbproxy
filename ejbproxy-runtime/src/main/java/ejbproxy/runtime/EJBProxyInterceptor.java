package ejbproxy.runtime;

import ejbproxy.runtime.configuration.RuntimeConfiguration;
import ejbproxy.runtime.configuration.jmx.RuntimeConfigurationChangeNotification;
import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationHandler;
import ejbproxy.runtime.invocation.InvocationLogger;
import ejbproxy.runtime.invocation.InvocationOnEJB;
import ejbproxy.runtime.invocation.InvocationOnEJBImpl;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import ejbproxy.runtime.util.EJBUtils;
import org.slf4j.Logger;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.List;

public class EJBProxyInterceptor implements NotificationListener,
		NotificationFilter {
	private static final long serialVersionUID = 1L;
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(EJBProxyInterceptor.class);

	private final RuntimeConfiguration config = RuntimeConfiguration.get();

	private String jndiBinding;

	private boolean proxyCalls;
	private InvocationHandler handler;
	private List<Endpoint> endpoints;
	private boolean loggingEnabled;
	private InvocationLogger logger;
	private Logger slf4jLogger;

	private boolean supported = true;
	private boolean initialized = false;
	private final Object configLock = new Object();

	@AroundInvoke
	public Object proxyCall(InvocationContext context) throws Exception {
		if (!initialized) {
			initialize(context.getTarget());
		}

		if (!supported) {
			return context.proceed();
		}

		InvocationOnEJB invocationOnEJB = new InvocationOnEJBImpl(jndiBinding,
				context.getTarget(), context.getMethod(),
				context.getParameters());

		if (proxyCalls) {
			if (loggingEnabled) {
				logger.logInit(slf4jLogger, invocationOnEJB);
			}
			try {
				Object result = handler.proxyInvocation(invocationOnEJB);
				if (loggingEnabled) {
					logger.logResult(slf4jLogger, invocationOnEJB, result);
				}
				return result;
			} catch (InvocationException invEx) {
				if (loggingEnabled) {
					logger.logException(
							slf4jLogger,
							invocationOnEJB,
							new InvocationException(
									invocationOnEJB,
									"Could not proxy ejb call! Switching into the default invocation pipeline.",
									invEx));
				} else {
					log.warn(
							"Could not proxy ejb call! Switching into the default invocation pipeline. Cause: {}",
							invEx.getMessage());
				}
				return context.proceed();
			} catch (Exception e) {
				if (loggingEnabled) {
					logger.logException(slf4jLogger, invocationOnEJB, e);
				}
				throw e;
			} catch (Throwable t) {
				// Should not occur
				throw new Exception(t);
			}
		} else {
			log.info("Unproxied ejb call! {}", invocationOnEJB);
			return context.proceed();
		}
	}

	// --- NotificationListener API ---

	@SuppressWarnings("unchecked")
	@Override
	public void handleNotification(Notification notification, Object handback) {
		RuntimeConfigurationChangeNotification cChangedNotification = (RuntimeConfigurationChangeNotification) notification;

		synchronized (configLock) {
			switch (cChangedNotification.getElement()) {
			case EJBPROXY_ENABLED:
				if ((Boolean) cChangedNotification.getNewValue() != proxyCalls) {
					proxyCalls = (Boolean) cChangedNotification.getNewValue();
				}
				break;
			case ENDPOINTS:
				if (!endpoints.equals(cChangedNotification.getNewValue())) {
					endpoints = config
							.getEndpointObjects(((Iterable<String>) cChangedNotification
									.getNewValue()));
				}
				handler.setEndpoints(endpoints);
				break;
			case HANDLER:
				if (!handler.equals(cChangedNotification.getNewValue())) {
					handler = config
							.newHandlerObject((String) cChangedNotification
									.getNewValue());
				}
				handler.setEndpoints(endpoints);
				break;
			case LOGGING_ENABLED:
				if ((Boolean) cChangedNotification.getNewValue() != loggingEnabled) {
					loggingEnabled = (Boolean) cChangedNotification
							.getNewValue();
				}
				break;
			case LOGGER:
				if (!logger.equals(cChangedNotification.getNewValue())) {
					logger = config
							.getLoggerObject((String) cChangedNotification
									.getNewValue());
				}
				break;
			case LOGGER_NAME:
				if (!slf4jLogger.equals(cChangedNotification.getNewValue())) {
					slf4jLogger = EJBProxyLoggerFactory
							.getLogger((String) cChangedNotification
									.getNewValue());
				}
				break;
			default:
				throw new AssertionError();
			}
		}
	}

	// --- NotificationFilter API ---

	@Override
	public boolean isNotificationEnabled(Notification notification) {
		if (notification instanceof RuntimeConfigurationChangeNotification) {
			if (RuntimeConfigurationChangeNotification.DEFAULT_CONFIGURATION_CHANGE_NOTIFICATION
					.equals(notification.getType())) {
				return true;
			} else if (RuntimeConfigurationChangeNotification.SPECIFIC_CONFIGURATION_CHANGE_NOTIFICATION
					.equals(notification.getType())
					&& ((RuntimeConfigurationChangeNotification) notification)
							.getJndiBinding().equals(jndiBinding)) {
				return true;
			}
		}

		return false;
	}

	// --- Utilities ---

	private void initialize(Object ejb) {
		synchronized (configLock) {
			if (!initialized) {
				jndiBinding = config.getComposerObject().composeGlobalJndiName(
						ejb);
				if (jndiBinding == null || !EJBUtils.isRemoteSessionBean(ejb)) {
					supported = false;
					return;
				}

				proxyCalls = config.isEJBProxyEnabled(jndiBinding) != null ? config
						.isEJBProxyEnabled(jndiBinding) : config
						.isEJBProxyEnabled();
				handler = config.newHandlerObject(config
						.getHandler(jndiBinding) != null ? config
						.getHandler(jndiBinding) : config.getDefaultHandler());
				endpoints = config.getEndpointObjects(config
						.getEndpoints(jndiBinding) != null ? config
						.getEndpoints(jndiBinding) : config
						.getDefaultEndpoints());
				handler.setEndpoints(endpoints);
				loggingEnabled = config.isLoggingEnabled(jndiBinding) != null ? config
						.isLoggingEnabled(jndiBinding) : config
						.isLoggingEnabled();
				logger = config
						.getLoggerObject(config.getLogger(jndiBinding) != null ? config
								.getLogger(jndiBinding) : config
								.getDefaultLogger());
				slf4jLogger = EJBProxyLoggerFactory.getLogger(config
						.getLoggerName(jndiBinding) != null ? config
						.getLoggerName(jndiBinding) : config
						.getDefaultLoggerName());

				config.addNotificationListener(this, this, null);

				initialized = true;
			}
		}
	}
}
