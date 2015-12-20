package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.basic.util.collection.LoopingIterator;
import ejbproxy.runtime.endpoint.Endpoint;
import ejbproxy.runtime.endpoint.Endpoint.Status;
import ejbproxy.runtime.invocation.AbstractInvocationHandler;
import ejbproxy.runtime.invocation.InvocationException;
import ejbproxy.runtime.invocation.InvocationOnEJB;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An handler which evenly load-balance invocations among all currently healthy
 * endpoints.
 */
public class LoadBalancingInvocationHandler extends AbstractInvocationHandler
		implements Endpoint.StatusListener {
	private boolean lookupPerformed;
	private InvocationException lookupException;

	private LoopingIterator<Pair<Endpoint, Object>> targets;

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
			lookupTargets(invocation);
		}

		if (lookupException != null) {
			throw lookupException;
		} else {
			return invokeTargetEJB(targets.next().getRight(), invocation);
		}
	}

	// --- Endpoint.StatusListener API ---

	@Override
	public void onStatusChanged(StatusChangedEvent statusEvent) {
		if (endpoints.contains(statusEvent.getEndpoint())) {
			lookupPerformed = false;
		}
	}

	// --- Utilities ---

	synchronized private void lookupTargets(InvocationOnEJB invocation)
			throws InvocationException {
		if (lookupPerformed) {
			return;
		} else {
			lookupPerformed = true;
			lookupException = null;
			targets = null;
		}

		if (!endpoints.isEmpty()) {
			Collection<Pair<Endpoint, Object>> ongoingTargets = new ArrayList<Pair<Endpoint, Object>>();

			for (Endpoint endpoint : endpoints) {
				if (endpoint.getStatus() == Status.OK) {
					try {
						ongoingTargets.add(new ImmutablePair<Endpoint, Object>(
								endpoint, endpoint.lookup(invocation
										.getEJBJndiBinding())));
						lookupException = null;
					} catch (NamingException e) {
						if (ongoingTargets.isEmpty()) {
							lookupException = new NoTargetEJBToCallException(
									invocation,
									String.format(
											"It was unable to lookup any target ejb for invocation %s from any available healthy endpoints! Endpoint urls were: %s",
											invocation,
											Utils.createEndpointUrlsString(endpoints)),
									e);
						}
					}
				}
			}

			if (ongoingTargets.isEmpty() && lookupException == null) {
				lookupException = new NoHealthyEndpointIsAvailableException(
						invocation, endpoints);
			}

			targets = new LoopingIterator<Pair<Endpoint, Object>>(
					ongoingTargets);
		} else {
			lookupException = new NoEndpointWasSetException(invocation);
		}
	}
}
