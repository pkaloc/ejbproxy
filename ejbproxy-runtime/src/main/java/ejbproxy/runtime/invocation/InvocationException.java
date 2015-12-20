package ejbproxy.runtime.invocation;

import ejbproxy.runtime.EJBProxyRuntimeException;
import org.apache.commons.lang3.Validate;

/**
 * Thrown by {@link InvocationHandler} every time an {@link InvocationOnEJB}
 * could not be forwarded/proxied for any reason.
 */
public class InvocationException extends EJBProxyRuntimeException {
	private static final long serialVersionUID = 1L;

	private final InvocationOnEJB invocation;

	public InvocationException(InvocationOnEJB invocation) {
		super();
		this.invocation = Validate.notNull(invocation);
	}

	public InvocationException(InvocationOnEJB invocation, String message) {
		super(message);
		this.invocation = Validate.notNull(invocation);
	}

	public InvocationException(InvocationOnEJB invocation, String message,
			Throwable cause) {
		super(message, cause);
		this.invocation = Validate.notNull(invocation);
	}

	public InvocationException(InvocationOnEJB invocation, Throwable cause) {
		super(cause);
		this.invocation = Validate.notNull(invocation);
	}

	public InvocationOnEJB getInvocation() {
		return invocation;
	}
}
