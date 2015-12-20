package ejbproxy.deploy;

import java.io.InputStream;
import java.io.OutputStream;

public interface EJBProxyDeployer {

	/**
	 * Creates an 'EJBProxyfied' enterprise archive using actual settings (path
	 * to the core EJBProxy archive, etc.) available to the
	 * <code>EJBProxyDeployer</code>.
	 * <p>
	 * Both streams are closed and there is no need to provide buffered
	 * variants.
	 * 
	 * @throws Exception
	 *             if a serious error preventing 'ejbproxyfying' occurs
	 */
	void ejbproxyfy(InputStream sourceEar, OutputStream targetEar)
			throws Exception;

	class Constants {
		public static final String EJBPROXY_CORE_ARCHIVE_FILE_NAME = "ejbproxy.jar";
	}
}
