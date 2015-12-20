package ejbproxy.deploy;

import org.apache.commons.lang3.Validate;

import java.net.URI;

public class WebJavaEEModuleDescriptor extends JavaEEModuleDescriptor {
	protected final String contextRoot;

	public WebJavaEEModuleDescriptor(URI uri, String contextRoot) {
		super(Type.WEB, uri);
		this.contextRoot = Validate.notNull(contextRoot);
	}

	public String getContextRoot() {
		return contextRoot;
	}
}
