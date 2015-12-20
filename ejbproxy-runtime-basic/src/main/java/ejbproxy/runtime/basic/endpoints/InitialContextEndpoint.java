package ejbproxy.runtime.basic.endpoints;

import ejbproxy.runtime.configuration.ConfigurationException;
import ejbproxy.runtime.endpoint.ConfigurableAbstractEndpoint;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * An endpoint which uses jndi {@linkplain InitialContext}.
 * <p>
 * The internal <code>InitialContext</code> is created during configuration with
 * environment containing <code>url</code> as {@link Context#PROVIDER_URL} and
 * all passed configuration params. If a <code>NamingException</code> is thrown,
 * its wrapped by <code>ConfigurationException</code> and rethrown to caller.
 * <p>
 * Implementations of the methods defined by the Endpoint interface are
 * thread-safe, for other methods´ thread-safety issues, please check the
 * {@linkplain InitialContext} class.
 */
public class InitialContextEndpoint extends ConfigurableAbstractEndpoint {
	private InitialContext initialContext;

	public InitialContextEndpoint(String url) {
		super(url);
	}

	public InitialContext getInitialContext() {
		return initialContext;
	}

	// --- Configurable API ---

	@Override
	public void configure(Map<String, String> configuration)
			throws ConfigurationException {
		super.configure(configuration);
		try {
			initialContext = new InitialContext(
					createInitialContextEnvironment(url, configuration));
		} catch (NamingException e) {
			throw new ConfigurationException(e);
		}
	}

	// --- Context API ---

	@Override
	public Object lookup(Name name) throws NamingException {
		return initialContext.lookup(name);
	}

	@Override
	public Object lookup(String name) throws NamingException {
		return initialContext.lookup(name);
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		initialContext.bind(name, obj);
	}

	@Override
	public void bind(String name, Object obj) throws NamingException {
		initialContext.bind(name, obj);
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		initialContext.rebind(name, obj);
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		initialContext.rebind(name, obj);
	}

	@Override
	public void unbind(Name name) throws NamingException {
		initialContext.unbind(name);
	}

	@Override
	public void unbind(String name) throws NamingException {
		initialContext.unbind(name);
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		initialContext.rename(oldName, newName);
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException {
		initialContext.rename(oldName, newName);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		return initialContext.list(name);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		return initialContext.list(name);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		return initialContext.listBindings(name);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		return initialContext.listBindings(name);
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {
		initialContext.destroySubcontext(name);
	}

	@Override
	public void destroySubcontext(String name) throws NamingException {
		initialContext.destroySubcontext(name);
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		return initialContext.createSubcontext(name);
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		return initialContext.createSubcontext(name);
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		return initialContext.lookupLink(name);
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		return initialContext.lookupLink(name);
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		return initialContext.getNameParser(name);
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		return initialContext.getNameParser(name);
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		return initialContext.composeName(name, prefix);
	}

	@Override
	public String composeName(String name, String prefix)
			throws NamingException {
		return initialContext.composeName(name, prefix);
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		return initialContext.addToEnvironment(propName, propVal);
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		return initialContext.removeFromEnvironment(propName);
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return initialContext.getEnvironment();
	}

	@Override
	public void close() throws NamingException {
		initialContext.close();
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		return initialContext.getNameInNamespace();
	}

	// --- Utilities ---

	private static Hashtable<String, String> createInitialContextEnvironment(
			String url, Map<String, String> configuration) {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(InitialContext.PROVIDER_URL, url);
		return env;
	}
}
