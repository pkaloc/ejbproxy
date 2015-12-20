package ejbproxy.runtime.invocation;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Method;
import java.util.Arrays;


public class InvocationOnEJBImpl implements InvocationOnEJB {
	private final String jndiBinding;
	private final Object ejb;
	private final Method method;
	private final Object[] args;

	public InvocationOnEJBImpl(String jndiBinding, Object ejb, Method method,
			Object[] args) {
		this.jndiBinding = Validate.notNull(jndiBinding);
		this.ejb = Validate.notNull(ejb);
		this.method = Validate.notNull(method);
		this.args = args == null ? new Object[0] : args;
	}

	@Override
	public String getEJBJndiBinding() {
		return jndiBinding;
	}

	@Override
	public Object getEJB() {
		return ejb;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public Object[] getArguments() {
		return args;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((ejb == null) ? 0 : ejb.hashCode());
		result = prime * result
				+ ((jndiBinding == null) ? 0 : jndiBinding.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvocationOnEJBImpl other = (InvocationOnEJBImpl) obj;
		if (!Arrays.equals(args, other.args))
			return false;
		if (ejb == null) {
			if (other.ejb != null)
				return false;
		} else if (!ejb.equals(other.ejb))
			return false;
		if (jndiBinding == null) {
			if (other.jndiBinding != null)
				return false;
		} else if (!jndiBinding.equals(other.jndiBinding))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[jndiBinding=" + jndiBinding + ", ejb=" + ejb + ", method="
				+ method + ", args=" + Arrays.toString(args) + "]";
	}
}
