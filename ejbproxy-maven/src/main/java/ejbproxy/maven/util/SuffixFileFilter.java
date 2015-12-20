package ejbproxy.maven.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

public class SuffixFileFilter implements FileFilter, FilenameFilter {
	private final String[] suffixes;

	public SuffixFileFilter(String... suffixes) {
		this.suffixes = suffixes;
	}

	public SuffixFileFilter(List<String> suffixes) {
		this.suffixes = suffixes.toArray(new String[suffixes.size()]);
	}

	public FilenameFilter getFilenameFilter() {
		return this;
	}

	public FileFilter getFileFilter() {
		return this;
	}

	@Override
	public boolean accept(File dir, String name) {
		for (String suffix : this.suffixes) {
			if (name.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean accept(File pathname) {
		String name = pathname.getName();
		for (String suffix : this.suffixes) {
			if (name.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

}
