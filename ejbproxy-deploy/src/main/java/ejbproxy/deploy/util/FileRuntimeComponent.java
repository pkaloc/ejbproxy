package ejbproxy.deploy.util;

import ejbproxy.deploy.RuntimeComponent;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A <code>RuntimeComponent</code> backed by {@link File}.
 */
public class FileRuntimeComponent implements RuntimeComponent {
	protected final String name;
	protected final File archive;

	public FileRuntimeComponent(String name, File archive) {
		this.name = Validate.notBlank(name);
		this.archive = Validate.notNull(archive);
		Validate.isTrue(archive.isFile());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream getArchive() throws IOException {
		return new FileInputStream(archive);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((archive == null) ? 0 : archive.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FileRuntimeComponent other = (FileRuntimeComponent) obj;
		if (archive == null) {
			if (other.archive != null)
				return false;
		} else if (!archive.equals(other.archive))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileRuntimeComponent [name=" + name + ", archive=" + archive
				+ "]";
	}
}
