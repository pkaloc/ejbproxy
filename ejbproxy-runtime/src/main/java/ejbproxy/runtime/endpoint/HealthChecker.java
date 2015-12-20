package ejbproxy.runtime.endpoint;

import ejbproxy.runtime.configuration.Configurable;

/**
 * Checks health of a given {@linkplain Endpoint} by setting its status.
 * <p>
 * Typical usage scenario is periodic calling of the {@linkplain #checkHealth()}
 * method. It is guaranteed that endpoints are set prior the first
 * {@link #checkHealth()} invocation and the
 * {@link Configurable#configure(java.util.Map)} method is called prior
 * endpoints are set, if the implementation is configurable.
 */
public interface HealthChecker {
	/**
	 * @return the endpoint which is being checked
	 */
	Endpoint getEndpoint();

	/**
	 * 
	 * @param endpoint
	 *            an endpoint which is gonna be checked. It must not be
	 *            <code>null</code>.
	 */
	void setEndpoint(Endpoint endpoint);

	/**
	 * Checks health of the set endpoint by setting its status.
	 */
	void checkHealth();
}
