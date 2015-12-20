package ejbproxy.runtime.configuration.util;

import ejbproxy.runtime.configuration.initial.elements.EndpointElement;
import ejbproxy.runtime.endpoint.HealthChecker;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class HealthCheckerThread extends Thread {
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(HealthCheckerThread.class);

	private final String CHECK_INTERVAL_CONFIG_PARAM_KEY = "checkinterval";
	private final int DEF_CHECK_INTERVAL = 60000;
	private int checkInterval;

	private final HealthChecker checker;

	private boolean interrupted = false;

	public HealthCheckerThread(EndpointElement endpointElement,
			HealthChecker checker) {
		super(new ThreadGroup(), "HealthChecker-"
				+ Validate.notNull(endpointElement).getName());
		setDaemon(true);
		this.checker = Validate.notNull(checker);

		try {
			checkInterval = Integer.parseInt(endpointElement
					.getHealthCheckerElement().getConfigParams()
					.get(CHECK_INTERVAL_CONFIG_PARAM_KEY));
		} catch (Exception e) {
			checkInterval = DEF_CHECK_INTERVAL;
		}
	}

	public HealthChecker getChecker() {
		return checker;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checker.checkHealth();

				long sleptTime = 0;
				long sleepingStartTime = 0;
				while (sleptTime < checkInterval - 200) {
					try {
						sleepingStartTime = System.currentTimeMillis();
						sleep(checkInterval - sleptTime);
					} catch (InterruptedException e) {
						interrupted = true;
					}
					sleptTime += System.currentTimeMillis() - sleepingStartTime;
				}
			}
		} catch (Exception e) {
			log.warn(
					"An unexpected exception was thrown during health check. Checker: {}",
					checker, e);
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public static class ThreadGroup extends java.lang.ThreadGroup {
		public ThreadGroup() {
			super("HealthCheckers");
		}
	}
}
