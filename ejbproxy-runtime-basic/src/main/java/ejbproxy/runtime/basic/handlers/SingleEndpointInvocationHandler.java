package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.endpoint.Endpoint.Status;
import ejbproxy.runtime.invocation.AbstractInvocationHandler;
import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationOnEJB;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An handler which always uses same single endpoint.
 */
public class SingleEndpointInvocationHandler extends AbstractInvocationHandler
		implements Endpoint.StatusListener {
	private boolean lookupPerformed;
	private InvocationException lookupException;

	private Pair<Endpoint, Object> target;

	@Override
	public void setEndpoints(List<Endpoint> endpoints) {
		if (super.getEndpoints() != null && !super.getEndpoints().isEmpty()) {
			super.getEndpoints().get(0).removeStatusListener(this);
		}

		Validate.noNullElements(endpoints);
		if (!endpoints.isEmpty()) {
			super.setEndpoints(Collections.singletonList(endpoints.get(0)));
			endpoints.get(0).addStatusListener(this);
		} else {
			super.setEndpoints(new ArrayList<Endpoint>());
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
		} else if (target.getLeft().getStatus() == Status.ERROR) {
			throw new NoHealthyEndpointIsAvailableException(invocation,
					endpoints);
		} else {
			return invokeTargetEJB(target.getRight(), invocation);
		}
	}

	// --- Endpoint.StatusListener API ---

	@Override
	public void onStatusChanged(StatusChangedEvent statusEvent) {
		if (statusEvent.getSource() == endpoints.get(0)) {
			if (statusEvent.getStatus() == Status.OK && lookupException != null) {
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
		if (!endpoints.isEmpty()) {
			if (endpoints.get(0).getStatus() == Status.OK) {
				try {
					targetEjb = endpoints.get(0).lookup(
							invocation.getEJBJndiBinding());
				} catch (NamingException e) {
					lookupException = new NoTargetEJBToCallException(
							invocation, e);
				}
			} else {
				lookupException = new NoHealthyEndpointIsAvailableException(
						invocation, endpoints);
			}
		} else {
			lookupException = new NoEndpointWasSetException(invocation);
		}

		target = new ImmutablePair<Endpoint, Object>(endpoints.get(0),
				targetEjb);
	}
}
