package ejbproxy.deploy;

import java.io.IOException;
import java.io.InputStream;


/**
 * Specifies a JavaEE module (archive file) to add into the resulting
 * 'ejbproxyfied' ear file during deployment of the EJBProxy.
 * <p>
 * It is recommended that the extra modules should not depend on the core
 * EJBProxy archive or even bundle it itself.
 * <p>
 * Please note that the extra modules will not be processed in any way during
 * deployment.
 */
public interface ExtraJavaEEModule {
	/**
	 * Gets the descriptor of the module.
	 * <p>
	 * The descriptor is needed because the deployment logic must know where to
	 * store this extra module and, if the JavaEE deployment descriptor exists
	 * in the source ear, also because of deployment descriptor modification
	 * purposes.
	 */
	JavaEEModuleDescriptor getDescriptor();

	/**
	 * Gets an <code>InputStream</code> which holds
	 * <code>ExtraJavaEEModule</code>´s data.
	 * <p>
	 * The caller must be able to call this method at least once. Subsequent
	 * invocations should either throw an exception or return a new instance.
	 * <p>
	 * It is caller responsibility to properly call this method and close the
	 * stream.
	 */
	InputStream getArchive() throws IOException;
}
