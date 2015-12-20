package ejbproxy.runtime.invocation;

import java.lang.reflect.Method;

/**
 * A logical representation of an invocation on an ejb.
 * <p>
 * This class is just a placeholder for ejb´s jndi binding, reference to the
 * ejb, the method that was called and the arguments that were passed.
 */
public interface InvocationOnEJB {
	String getEJBJndiBinding();

	Object getEJB();

	Method getMethod();

	Object[] getArguments();
}