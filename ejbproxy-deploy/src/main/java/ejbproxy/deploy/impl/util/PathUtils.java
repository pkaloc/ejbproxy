package ejbproxy.deploy.impl.util;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class PathUtils {
	public static String getRelativeToPath(File root, File source, File target) {
		StringBuilder path = new StringBuilder();

		source = source.getParentFile();
		for (; !root.equals(source); source = source.getParentFile()) {
			path.append("../");
		}

		Deque<String> targetParents = new ArrayDeque<String>();
		File parent = target.getParentFile();
		for (; !root.equals(parent); parent = parent.getParentFile()) {
			targetParents.push(parent.getName());
		}
		for (String targetParentName : targetParents) {
			path.append(targetParentName);
			path.append("/");
		}
		path.append(target.getName());

		return path.toString();
	}
}
