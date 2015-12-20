package ejbproxy.runtime.endpoint;

import ejbproxy.runtime.configuration.Configurable;
import org.apache.commons.lang3.Validate;

import javax.naming.Context;
import java.util.EventObject;

/**
 * Represents an EJB container which can be used to lookup its enterprise beans.
 * <p>
 * Basically it is wrapped jndi {@link Context}, so one can perform all
 * operations provided by this interface or lookup whatever what is provided by
 * this container.
 * <p>
 * It can be in two states, <code>OK</code> (the default) and <code>ERROR</code>
 * . When it is in the ERROR status, the EJB container should not be used until
 * it becomes <code>OK</code> again.
 * <p>
 * It is guaranteed that the {@link Configurable#configure(java.util.Map)}
 * method is called prior any business method, if the implementation is
 * configurable.
 * <p>
 * Implementations should be thread-safe because may be used simultaneously by
 * many threads. When it comes to the <code>Context</code> interface, most
 * likely the <code>lookup</code> method will be the only one used.
 */
public interface Endpoint extends Context {
	enum Status {
		OK, ERROR
	}

	String getURL();

	Status getStatus();

	void setStatus(Status newStatus);

	void addStatusListener(StatusListener listener);

	void removeStatusListener(StatusListener listener);

	interface StatusListener {
		void onStatusChanged(StatusChangedEvent statusEvent);

		class StatusChangedEvent extends EventObject {
			private static final long serialVersionUID = 1L;

			private final Status status;

			public StatusChangedEvent(Endpoint source, Status status) {
				super(source);
				this.status = Validate.notNull(status);
			}

			public Endpoint getEndpoint() {
				return (Endpoint) source;
			}

			public Status getStatus() {
				return status;
			}
		}
	}
}
