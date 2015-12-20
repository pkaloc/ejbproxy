package ejbproxy.runtime.invocation;

import ejbproxy.runtime.configuration.Configurable;
import org.slf4j.Logger;

/**
 * Logs invocations upon ejbs (no matter whether real or proxied) on the
 * <code>TRACE</code> level.
 * <p/>
 * New invocations are logged by the {@link #logInit(Logger, InvocationOnEJB)}
 * method, invocation results and possible exceptions thrown by the
 * {@link #logResult(Logger, InvocationOnEJB, Object)} and
 * {@link #logException(Logger, InvocationOnEJB, Throwable)} respectively.
 * <p/>
 * It is guaranteed that the {@link Configurable#configure(java.util.Map)}
 * method is called prior any <code>logXXX</code> invocation, if the
 * implementation is configurable. It is also guaranteed that every
 * <code>logInit</code> and subsequent <code>logResult|logException</code>
 * invocation pair is performed on the same thread. So, implementations can use
 * any features using <code>ThreadLocal</code> or so. For instance, the MDC
 * feature. For more information please look at <a href="http://www.slf4j.org/manual.html#mdc">this</a> webpage.
 * <p/>
 * Implementations must be thread-safe because may be used simultaneously by
 * many threads. Best, should be idempotent and so does not used any
 * synchronization techniques to ensure thread-safety.
 */
public interface InvocationLogger {
	void logInit(Logger logger, InvocationOnEJB invocation);

	void logResult(Logger logger, InvocationOnEJB invocation, Object result);

	void logException(Logger logger, InvocationOnEJB invocation,
	                  Throwable throwable);
}