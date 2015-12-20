package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.endpoint.Endpoint.Status;
import ejbproxy.runtime.invocation.AbstractInvocationHandler;
import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationOnEJB;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.naming.NamingException;
import java.util.List;

/**
 * A handler which always uses same endpoint until its status is <code>OK</code>
 * . When its status becomes <code>ERROR</code>, it is switched to another
 * healthy endpoint, if available.
 */
public class FailoveringInvocationHandler extends AbstractInvocationHandler
		implements Endpoint.StatusListener {

	private boolean lookupPerformed;
	private InvocationException lookupException;

	private Pair<Endpoint, Object> target;

	@Override
	public void setEndpoints(List<Endpoint> endpoints) {
		if (super.getEndpoints() != null) {
			for (Endpoint endpoint : super.getEndpoints()) {
				endpoint.removeStatusListener(this);
			}
		}
		super.setEndpoints(endpoints);

		for (Endpoint endpoint : endpoints) {
			endpoint.addStatusListener(this);
		}
		lookupPerformed = false;
	}

	@Override
	public Object proxyInvocation(InvocationOnEJB invocation) throws Throwable {
		if (!lookupPerformed) {
			lookupTarget(invocation);
		}

		if (lookupException != null) {
			throw lookupException;
		} else {
			return invokeTargetEJB(target.getRight(), invocation);
		}
	}

	// --- Endpoint.StatusListener API ---

	@Override
	public void onStatusChanged(StatusChangedEvent statusEvent) {
		if (endpoints.contains(statusEvent.getEndpoint())) {
			if (statusEvent.getStatus() == Status.ERROR && target != null
					&& target.getLeft() == statusEvent.getSource()) {
				lookupPerformed = false;
			} else if (statusEvent.getStatus() == Status.OK
					&& lookupException != null) {
				lookupPerformed = false;
			}
		}
	}

	// --- Utilities ---

	synchronized private void lookupTarget(InvocationOnEJB invocation)
			throws InvocationException {
		if (lookupPerformed) {
			return;
		} else {
			lookupPerformed = true;
			lookupException = null;
			target = null;
		}

		Object targetEjb = null;
		Endpoint chosenEndpoint = null;
		if (!endpoints.isEmpty()) {
			for (Endpoint endpoint : endpoints) {
				if (endpoint.getStatus() == Status.OK) {
					try {
						targetEjb = endpoint.lookup(invocation
								.getEJBJndiBinding());
						chosenEndpoint = endpoint;
						lookupException = null;
						break;
					} catch (NamingException e) {
						lookupException = new NoTargetEJBToCallException(
								invocation,
								String.format(
										"It was unable to lookup target ejb for invocation %s from any available healthy endpoints! Endpoint urls were: %s",
										invocation,
										Utils.createEndpointUrlsString(endpoints)),
								e);
					}
				}
			}

			if (targetEjb == null && lookupException == null) {
				lookupException = new NoHealthyEndpointIsAvailableException(
						invocation, endpoints);
			}
		} else {
			lookupException = new NoEndpointWasSetException(invocation);
		}

		target = new ImmutablePair<Endpoint, Object>(chosenEndpoint, targetEjb);
	}
}
