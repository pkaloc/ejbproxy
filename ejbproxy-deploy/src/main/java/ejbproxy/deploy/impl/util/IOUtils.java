package ejbproxy.deploy.impl.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
	public static IOException safeClose(Closeable closeable) {
		try {
			closeable.close();
			return null;
		} catch (IOException e) {
			return e;
		}
	}
}
