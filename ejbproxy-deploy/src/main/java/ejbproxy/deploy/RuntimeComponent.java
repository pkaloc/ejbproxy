package ejbproxy.deploy;

import java.io.IOException;
import java.io.InputStream;


/**
 * The core of EJBProxy may be extended with another runtime components (jar
 * archives). These components will typically bundle implementations of
 * endpoints, invocation handlers, loggers etc.
 * <p>
 * To let EJBProxy be able to work with these implementations, every runtime
 * component is either bundled into the core EJBProxy archive or referenced in
 * the manifest file during deployment.
 * <p>
 * It is recommended that the runtime components should not depend on the core
 * EJBProxy archive or even bundle it itself.
 */
public interface RuntimeComponent {
	/**
	 * The name of the component.
	 * <p>
	 * Used when the component is being bundled by reference. In this case, the
	 * component is bundled as a jar archive named by this <code>name</code> and
	 * stored in the <code>targetPath</code>. See
	 * {@link DeploymentSettings#getTargetPath()} for more details.
	 */
	String getName();

	/**
	 * Gets an <code>InputStream</code> which holds
	 * <code>RuntimeComponent</code>´s data.
	 * <p>
	 * The caller must be able to call this method at least once. Subsequent
	 * invocations should either throw an exception or return a new instance.
	 * <p>
	 * It is caller responsibility to properly call this method and close the
	 * stream.
	 */
	InputStream getArchive() throws IOException;
}
