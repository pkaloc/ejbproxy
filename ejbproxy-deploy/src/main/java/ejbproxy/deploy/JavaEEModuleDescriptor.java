package ejbproxy.deploy;

import org.apache.commons.lang3.Validate;

import java.net.URI;

/**
 * Logical representation of EJB or WEB JavaEE module´s deployment descriptor
 * definition.
 * <p>
 * Other types of JavaEE modules are not supported because cannot hold EJBs.
 * Therefore are useless for EJBProxy.
 */
public class JavaEEModuleDescriptor {
	public enum Type {
		EJB, WEB, JAVA, CONNECTOR
	}

	protected final Type type;
	protected final URI uri;

	public JavaEEModuleDescriptor(Type type, URI uri) {
		this.type = Validate.notNull(type);
		this.uri = Validate.notNull(uri);
	}

	public Type getType() {
		return type;
	}

	public URI getURI() {
		return uri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaEEModuleDescriptor other = (JavaEEModuleDescriptor) obj;
		if (type != other.type)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaEEModuleDescriptor [type=" + type + ", uri=" + uri + "]";
	}
}
