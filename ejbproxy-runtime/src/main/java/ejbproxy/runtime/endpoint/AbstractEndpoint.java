package ejbproxy.runtime.endpoint;

import ejbproxy.runtime.endpoint.Endpoint.StatusListener.StatusChangedEvent;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEndpoint implements Endpoint {
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(AbstractEndpoint.class);

	protected final String url;

	protected Status status = Status.OK;
	protected final List<StatusListener> statusListeners;

	public AbstractEndpoint(String url) {
		this.url = url;
		statusListeners = new ArrayList<Endpoint.StatusListener>();
	}

	// --- Endpoint API ---

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	synchronized public void setStatus(Status newStatus) {
		if (Validate.notNull(newStatus) != status) {
			status = newStatus;
		}
		try {
			notifyStatusListeners();
		} catch (RuntimeException e) {
			log.warn(
					"An unexpected exception was thrown during endpoint´s listeners notification.",
					e);
		}
	}

	@Override
	synchronized public void addStatusListener(StatusListener listener) {
		statusListeners.add(Validate.notNull(listener));
	}

	@Override
	synchronized public void removeStatusListener(StatusListener listener) {
		statusListeners.remove(Validate.notNull(listener));
	}

	// --- Utilities ---

	private void notifyStatusListeners() {
		StatusChangedEvent event = new StatusChangedEvent(this, status);
		for (StatusListener listener : statusListeners) {
			listener.onStatusChanged(event);
		}
	}
}
