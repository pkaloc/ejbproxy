package ejbproxy.deploy.util;

import ejbproxy.deploy.ExtraJavaEEModule;
import ejbproxy.deploy.JavaEEModuleDescriptor;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An <code>ExtraJavaEEModule</code> backed by {@link File}.
 */
public class FileExtraJavaEEModule implements ExtraJavaEEModule {
	protected final JavaEEModuleDescriptor descriptor;
	protected final File archive;

	public FileExtraJavaEEModule(JavaEEModuleDescriptor descriptor, File archive) {
		this.descriptor = Validate.notNull(descriptor);
		this.archive = Validate.notNull(archive);
		Validate.isTrue(archive.isFile());
	}

	@Override
	public JavaEEModuleDescriptor getDescriptor() {
		return descriptor;
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
		result = prime * result
				+ ((descriptor == null) ? 0 : descriptor.hashCode());
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
		FileExtraJavaEEModule other = (FileExtraJavaEEModule) obj;
		if (archive == null) {
			if (other.archive != null)
				return false;
		} else if (!archive.equals(other.archive))
			return false;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileExtraJavaEEModule [descriptor=" + descriptor + ", archive="
				+ archive + "]";
	}
}
