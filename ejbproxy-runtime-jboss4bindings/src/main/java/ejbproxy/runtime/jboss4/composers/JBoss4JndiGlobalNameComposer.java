package ejbproxy.runtime.jboss4.composers;

import ejbproxy.runtime.configuration.Configurable;
import ejbproxy.runtime.jboss4.JBossRuntime;
import ejbproxy.runtime.logging.EJBProxyLoggerFactory;
import ejbproxy.runtime.util.jndi.JndiGlobalNameComposer;
import org.apache.commons.lang3.Validate;
import org.jboss.annotation.ejb.RemoteBinding;
import org.slf4j.Logger;

import java.util.Map;

/**
 * JBoss4 jndi global name composer.
 * 
 * @see <a href="http://docs.jboss.org/ejb3/app-server/tutorial/jndibinding/jndi.html">
 *     http://docs.jboss.org/ejb3/app-server/tutorial/jndibinding/jndi.html</a>
 */
public class JBoss4JndiGlobalNameComposer implements JndiGlobalNameComposer,
		Configurable {
	private static final Logger log = EJBProxyLoggerFactory
			.getLogger(JBoss4JndiGlobalNameComposer.class);

	private static final String EAR_NAME_KEY = "earname";

	private String earName;

	@Override
	public void configure(Map<String, String> configuration) {
		if (configuration.containsKey(EAR_NAME_KEY)) {
			earName = configuration.get(EAR_NAME_KEY);
			if (earName.endsWith(".ear")) {
				earName = earName.substring(0, earName.length() - 4);
			}
		}
	}

	@Override
	public String composeGlobalJndiName(Object ejb) {
		Validate.notNull(ejb);

		RemoteBinding customBinding = ejb.getClass().getAnnotation(
				RemoteBinding.class);
		if (customBinding != null && !customBinding.jndiBinding().isEmpty()) {
			return customBinding.jndiBinding();
		} else if (earName != null) {
			return earName + "/" + ejb.getClass().getSimpleName() + "/remote";
		} else {
			return ejb.getClass().getSimpleName() + "/remote";
		}
	}

	static {
		String jbossVersion = JBossRuntime.getVersionString();
		if (jbossVersion == null) {
			log.warn("Unable to check current JBoss version. Please note that JBoss4JndiGlobalNameComposer may not work correctly on other then JBoss4 runtime!");
		} else if (!jbossVersion.startsWith("4")) {
			log.warn("Unsupported JBoss runtime possibly used. Please note that JBoss4JndiGlobalNameComposer may not work correctly on other than JBoss4 runtime!");
		}
	}
}
