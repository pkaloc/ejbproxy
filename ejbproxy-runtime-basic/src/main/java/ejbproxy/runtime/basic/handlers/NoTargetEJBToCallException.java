package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationHandler;
import ejbproxy.runtime.invocation.InvocationOnEJB;



/**
 * Thrown by {@link InvocationHandler} every time an {@link InvocationOnEJB}
 * could not be forwarded/proxied because no target ejb is currently available
 * for the given endpoint. This may happen if a lookup on a healthy endpoint
 * throws exception, for instance.
 */
public class NoTargetEJBToCallException extends InvocationException {
	private static final long serialVersionUID = 1L;

	private static final String DEF_EX_MSG = "No target EJB was available for invocation '%s'.";

	public NoTargetEJBToCallException(InvocationOnEJB invocation) {
		super(invocation, String.format(DEF_EX_MSG, invocation));
	}

	public NoTargetEJBToCallException(InvocationOnEJB invocation, String message) {
		super(invocation, message);
	}

	public NoTargetEJBToCallException(InvocationOnEJB invocation,
			String message, Throwable cause) {
		super(invocation, message, cause);
	}

	public NoTargetEJBToCallException(InvocationOnEJB invocation,
			Throwable cause) {
		super(invocation, String.format(DEF_EX_MSG, invocation), cause);
	}
}