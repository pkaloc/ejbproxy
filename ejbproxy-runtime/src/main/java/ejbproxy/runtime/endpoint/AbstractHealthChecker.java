package ejbproxy.runtime.endpoint;

import org.apache.commons.lang3.Validate;

public abstract class AbstractHealthChecker implements HealthChecker {
	protected Endpoint endpoint;

	@Override
	public Endpoint getEndpoint() {
		return endpoint;
	}

	@Override
	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = Validate.notNull(endpoint);
	}
}
