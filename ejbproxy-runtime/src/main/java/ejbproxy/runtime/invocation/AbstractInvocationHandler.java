package ejbproxy.runtime.invocation;

import ejbproxy.runtime.endpoint.Endpoint;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractInvocationHandler implements InvocationHandler {
	protected List<Endpoint> endpoints;

	@Override
	public List<Endpoint> getEndpoints() {
		return endpoints == null ? new ArrayList<Endpoint>() : Collections
				.unmodifiableList(endpoints);
	}

	@Override
	public void setEndpoints(List<Endpoint> endpoints) {
		this.endpoints = new ArrayList<Endpoint>(
				Validate.noNullElements(endpoints));
	}

	protected Object invokeTargetEJB(Object targetEJB,
			InvocationOnEJB invocation) throws Throwable {
		try {
			return MethodUtils.invokeMethod(targetEJB, invocation.getMethod()
					.getName(), invocation.getArguments());
		} catch (NoSuchMethodException e) {
			throw new WrongTargetEJBException(targetEJB, invocation, e);
		} catch (IllegalAccessException e) {
			throw new WrongTargetEJBException(targetEJB, invocation, e);
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				throw e.getCause();
			} else {
				throw e;
			}
		}
	}
}
