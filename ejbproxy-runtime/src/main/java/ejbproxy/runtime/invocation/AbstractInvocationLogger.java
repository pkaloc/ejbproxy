package ejbproxy.runtime.invocation;

import org.slf4j.Logger;

public abstract class AbstractInvocationLogger implements InvocationLogger {
	protected abstract String doLogInit(InvocationOnEJB invocation);

	protected abstract String doLogResult(InvocationOnEJB invocation,
			Object result);

	protected abstract String doLogException(InvocationOnEJB invocation,
			Throwable throwable);

	// --- InvocationLogger API ---

	@Override
	public void logInit(Logger logger, InvocationOnEJB invocation) {
		if (logger.isTraceEnabled()) {
			logger.trace(doLogInit(invocation));
		}
	}

	@Override
	public void logResult(Logger logger, InvocationOnEJB invocation,
			Object result) {
		if (logger.isTraceEnabled()) {
			logger.trace(doLogResult(invocation, result));
		}
	}

	@Override
	public void logException(Logger logger, InvocationOnEJB invocation,
			Throwable throwable) {
		if (logger.isTraceEnabled()) {
			logger.trace(doLogException(invocation, throwable));
		}
	}
}
