package ejbproxy.runtime.util;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

public class EJBUtils {
	public static boolean isRemoteSessionBean(Object ejb) {
		Class<?> ejbClass = ejb.getClass();

		Stateless stateless = ejbClass.getAnnotation(Stateless.class);
		Stateful stateful = ejbClass.getAnnotation(Stateful.class);
		if (stateless != null || stateful != null) {
			if (ejbClass.getAnnotation(Remote.class) == null) {
				for (Class<?> ejbInterface : ejbClass.getInterfaces()) {
					if (ejbInterface.getAnnotation(Remote.class) != null) {
						return true;
					}
				}
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
}
