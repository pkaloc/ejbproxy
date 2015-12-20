package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationHandler;
import ejbproxy.runtime.invocation.InvocationOnEJB;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Thrown by {@link InvocationHandler} every time an {@link InvocationOnEJB}
 * could not be forwarded/proxied because no healthy endpoint is available to
 * the given endpoint.
 */
public class NoHealthyEndpointIsAvailableException extends InvocationException {
	private static final long serialVersionUID = 1L;

	public NoHealthyEndpointIsAvailableException(InvocationOnEJB invocation,
			List<Endpoint> endpoints) {
		super(
				Validate.notNull(invocation),
				String.format(
						"No healthy endpoint was available for ejb with jndi binding: '%s'. Endpoint urls were: %s",
						invocation.getEJBJndiBinding(), Utils
								.createEndpointUrlsString(Validate
										.noNullElements(endpoints))));
	}
}
