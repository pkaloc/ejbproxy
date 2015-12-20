package ejbproxy.runtime.configuration.initial;

import ejbproxy.runtime.configuration.RuntimeConfiguration;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Holds the pointer to the <code>InitialConfiguration</code> implementation.
 * <p>
 * This pointer is used by other components willing to use the
 * <code>InitialConfiguration</code> API throughout EJBProxy. Especially by the
 * {@link RuntimeConfiguration} which loads its initial configuration from it.
 * <p>
 * The implementation is loaded with the aid of the standard
 * {@link ServiceLoader} functionality. If no implementation cannot be found
 * this way, the {@link DefaultInitialConfiguration} is used then.
 * 
 * @see DefaultInitialConfiguration
 */
public class InitialConfigurationHolder {
	public static InitialConfiguration getConfiguration() {
		return InstanceHolder.instance;
	}

	private static class InstanceHolder {
		public static final InitialConfiguration instance = chooseInitialConfigImpl();

		private static InitialConfiguration chooseInitialConfigImpl() {
			Iterator<InitialConfiguration> iniConfigCustomImpls = ServiceLoader
					.load(InitialConfiguration.class,
							InitialConfiguration.class.getClassLoader())
					.iterator();

			if (iniConfigCustomImpls.hasNext()) {
				return iniConfigCustomImpls.next();
			} else {
				return new DefaultInitialConfiguration();
			}
		}
	}
}
