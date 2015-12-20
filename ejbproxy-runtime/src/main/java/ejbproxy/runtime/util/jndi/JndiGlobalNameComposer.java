package ejbproxy.runtime.util.jndi;

import ejbproxy.runtime.configuration.Configurable;

/**
 * Composes ejbs jndi bindings at runtime.
 * <p>
 * It is guaranteed that the {@link Configurable#configure(java.util.Map)}
 * method is called prior any <code>composeGlobalJndiName</code> invocation, if
 * the implementation is configurable.
 * <p>
 * Implementations must be thread-safe because may be used simultaneously by
 * many threads. Best, should be idempotent and so does not used any
 * synchronization techniques to ensure thread-safety.
 * <p>
 * Technical note: To correctly compose a jndi binding of an ejb at runtime may
 * be tricky without various additional information. For now, all additional
 * information must be either retrieved by the implementation itself or
 * statically provided thru configuration parameters by the deployer in the
 * configuration file. In future versions, various information retrieved from
 * the enclosing ear´s descriptors may be also provided, for instance.
 */
public interface JndiGlobalNameComposer {
	/**
	 * Returns composed global jndi name for the passed ejb object.
	 * <p>
	 * If <code>null</code> is returned, the EJBProxy runtime will never try to
	 * forward/proxy invocations upon the passed ejb object. So, the
	 * <code>JndiGlobalNameComposer</code> can also behave as a filter.
	 */
	String composeGlobalJndiName(Object ejb);
}