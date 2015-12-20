package ejbproxy.maven.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public class TrueFileFilter implements FileFilter, FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		return true;
	}

	@Override
	public boolean accept(File pathname) {
		return true;
	}

	public FilenameFilter getFilenameFilter() {
		return this;
	}

	public FileFilter getFileFilter() {
		return this;
	}
}
