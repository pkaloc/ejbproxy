package ejbproxy.runtime.invocation;

import ejbproxy.runtime.configuration.Configurable;
import ejbproxy.runtime.endpoint.Endpoint;

import java.util.List;

/**
 * Handles invocations upon a given ejb by forwarding (proxying) them to the
 * target ejb(s) and returning back results.
 * <p>
 * It is guaranteed that every invocation is upon the same ejb. That means that
 * all {@linkplain InvocationOnEJB} instances will always point to the same jndi
 * binding and ejb object. What will differ will be the method and the
 * arguments, of course. It is also guaranteed that endpoints are set prior the
 * first {@link #proxyInvocation(InvocationOnEJB)} invocation and the
 * {@link Configurable#configure(java.util.Map)} method is called prior
 * endpoints are set, if the implementation is configurable.
 * <p>
 * Implementations must not invoke any method on passed ejb objects and should
 * not log anything. All successes/failures should be expressed by results/
 * {@linkplain InvocationException} exceptions only.
 * <p>
 * Endpoints may change from time to time. That means the
 * {@link #setEndpoints(List)} method may be called more than once.
 * <p>
 * Implementations must be thread-safe.
 */
public interface InvocationHandler {
	List<Endpoint> getEndpoints();

	void setEndpoints(List<Endpoint> endpoints);

	/**
	 * Forwards/proxies an invocation upon source ejb to another (target) ejb.
	 * 
	 * @param invocation
	 *            invocation upon ejb
	 * @return result from the target ejb invocation
	 * @throws Throwable
	 *             an exception thrown by the target ejb invocation or
	 *             {@link InvocationException}. No other exceptions should be
	 *             thrown.
	 */
	Object proxyInvocation(InvocationOnEJB invocation) throws Throwable;
}
