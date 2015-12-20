package ejbproxy.runtime.invocation;

import org.apache.commons.lang3.Validate;

/**
 * May be thrown by {@link InvocationHandler} every time an
 * {@link InvocationOnEJB} could not be forwarded/proxied because the target ejb
 * does not conform to the source ejb´s api. That is does not define the method
 * returned by {@link InvocationOnEJB#getMethod()}.
 */
public class WrongTargetEJBException extends InvocationException {
	private static final long serialVersionUID = 1L;
	private static final String DEF_EX_MSG = "Target EJB '%s' for invocation '%s' is wrong!";

	private final Object targetEJB;

	public WrongTargetEJBException(Object targetEJB, InvocationOnEJB invocation) {
		super(invocation, String.format(DEF_EX_MSG,
				Validate.notNull(targetEJB), invocation));
		this.targetEJB = targetEJB;
	}

	public WrongTargetEJBException(Object targetEJB,
			InvocationOnEJB invocation, String message) {
		super(invocation, message);
		this.targetEJB = targetEJB;
	}

	public WrongTargetEJBException(Object targetEJB,
			InvocationOnEJB invocation, String message, Throwable cause) {
		super(invocation, message, cause);
		this.targetEJB = targetEJB;
	}

	public WrongTargetEJBException(Object targetEJB,
			InvocationOnEJB invocation, Throwable cause) {
		super(invocation, String.format(DEF_EX_MSG, targetEJB, invocation),
				cause);
		this.targetEJB = targetEJB;
	}

	public Object getTargetEJB() {
		return targetEJB;
	}
}