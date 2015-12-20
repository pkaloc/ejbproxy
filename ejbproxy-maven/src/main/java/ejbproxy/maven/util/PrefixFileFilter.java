package ejbproxy.maven.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

public class PrefixFileFilter implements FileFilter, FilenameFilter {
	private final String[] prefixes;

	public PrefixFileFilter(String... prefixes) {
		this.prefixes = prefixes;
	}

	public PrefixFileFilter(List<String> prefixes) {
		this.prefixes = prefixes.toArray(new String[prefixes.size()]);
	}

	public FilenameFilter getFilenameFilter() {
		return this;
	}

	public FileFilter getFileFilter() {
		return this;
	}

	@Override
	public boolean accept(File dir, String name) {
		for (String prefix : this.prefixes) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean accept(File pathname) {
		String name = pathname.getName();
		for (String prefix : this.prefixes) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

}
