package ejbproxy.runtime.basic.handlers;

import ejbproxy.runtime.endpoint.Endpoint;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;

class Utils {
	public static String createEndpointUrlsString(List<Endpoint> endpoints) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < endpoints.size(); i++) {
			try {
				result.append(endpoints.get(i).getEnvironment()
						.get(Context.PROVIDER_URL));
			} catch (NamingException e) {
				result.append("<error_while_fetching>");
			}

			if (i < endpoints.size() - 1) {
				result.append(", ");
			}
		}

		return result.toString();
	}
}
