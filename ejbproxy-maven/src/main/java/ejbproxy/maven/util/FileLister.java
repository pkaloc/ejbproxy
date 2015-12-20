package ejbproxy.maven.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileLister {
	protected final File root;
	protected final FileFilter fileFilter;
	protected final int maxDepth;

	public FileLister(File root) {
		this(root, Integer.MAX_VALUE);
	}

	public FileLister(File root, int maxDepth) {
		this(root, maxDepth, new TrueFileFilter());
	}

	public FileLister(File root, FileFilter fileFilter) {
		this(root, Integer.MAX_VALUE, fileFilter);
	}

	public FileLister(File root, int maxDepth, FileFilter fileFilter) {
		if (root == null) {
			throw new NullPointerException();
		}
		this.root = root;
		this.fileFilter = fileFilter == null ? new TrueFileFilter()
				: fileFilter;
		this.maxDepth = maxDepth;
	}

	public List<File> listFiles() {
		List<File> results = new ArrayList<File>();
		doListFiles(root, 0, results);
		return results;
	}

	private void doListFiles(File root, int depth, List<File> results) {
		for (File file : root.listFiles()) {
			if (fileFilter.accept(file)) {
				results.add(file);
			}
			if (file.isDirectory() && !((depth + 1) > maxDepth)) {
				doListFiles(file, depth + 1, results);
			}
		}
	}
}
