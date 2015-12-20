package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationHandler;
import ejbproxy.runtime.invocation.InvocationOnEJB;



/**
 * Thrown by {@link InvocationHandler} every time an {@link InvocationOnEJB}
 * could not be forwarded/proxied because no endpoint was passed to the given
 * endpoint.
 */
public class NoEndpointWasSetException extends InvocationException {
	private static final long serialVersionUID = 1L;

	public NoEndpointWasSetException(InvocationOnEJB invocation) {
		super(invocation, String.format(
				"No endpoint was set for ejb with jndi binding: '%s'.",
				invocation.getEJBJndiBinding()));
	}

}
