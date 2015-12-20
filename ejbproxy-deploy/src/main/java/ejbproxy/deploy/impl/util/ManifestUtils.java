package ejbproxy.deploy.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestUtils {
	public static final String CLASS_PATH_ATTR_ITEM_SEPARATOR = " ";

	/**
	 * Equivalent to {@link #augmentClassPath(Manifest, boolean, String...)
	 * augmentClassPath(manifest, true, entriesToAdd)}
	 */
	public static Manifest augmentClassPath(Manifest manifest,
			String... entriesToAdd) {
		return augmentClassPath(manifest, true, entriesToAdd);
	}

	public static Manifest augmentClassPath(Manifest manifest,
			boolean addToStart, String... entriesToAdd) {
		String resultingClassPath = StringUtils.join(
				Validate.noNullElements(entriesToAdd),
				CLASS_PATH_ATTR_ITEM_SEPARATOR);

		if (manifest.getMainAttributes()
				.containsKey(Attributes.Name.CLASS_PATH)) {
			if (addToStart) {
				resultingClassPath += CLASS_PATH_ATTR_ITEM_SEPARATOR;
				resultingClassPath += manifest.getMainAttributes().getValue(
						Attributes.Name.CLASS_PATH);
			} else {
				String temp = resultingClassPath;
				resultingClassPath = manifest.getMainAttributes().getValue(
						Attributes.Name.CLASS_PATH);
				resultingClassPath += CLASS_PATH_ATTR_ITEM_SEPARATOR;
				resultingClassPath += temp;
			}
		}
		manifest.getMainAttributes().putValue(
				Attributes.Name.CLASS_PATH.toString(), resultingClassPath);

		return manifest;
	}
}
