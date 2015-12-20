package ejbproxy.runtime;

/**
 * Root EJBProxy-Runtime exception.
 */
public class EJBProxyRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EJBProxyRuntimeException() {
		super();
	}

	public EJBProxyRuntimeException(String message) {
		super(message);
	}

	public EJBProxyRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public EJBProxyRuntimeException(Throwable cause) {
		super(cause);
	}
}
