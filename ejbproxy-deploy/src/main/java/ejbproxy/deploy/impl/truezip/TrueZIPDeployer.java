package ejbproxy.deploy.impl.truezip;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.io.Streams;
import ejbproxy.deploy.DeploymentSettings;
import ejbproxy.deploy.EJBProxyDeployer;
import ejbproxy.deploy.ExtraJavaEEModule;
import ejbproxy.deploy.JavaEEModuleDescriptor;
import ejbproxy.deploy.JavaEEModuleDescriptor.Type;
import ejbproxy.deploy.RuntimeComponent;
import ejbproxy.deploy.impl.util.EJBDDUtils;
import ejbproxy.deploy.impl.util.IOUtils;
import ejbproxy.deploy.impl.util.JavaEEDDUtils;
import ejbproxy.deploy.impl.util.ManifestUtils;
import ejbproxy.deploy.impl.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

public class TrueZIPDeployer implements EJBProxyDeployer {
	private static final Logger log = LoggerFactory
			.getLogger(TrueZIPDeployer.class);

	protected final DeploymentSettings settings;
	protected final List<RuntimeComponent> runtimeComponents;
	protected final List<ExtraJavaEEModule> extraModules;

	protected final InputStream ejbProxyArchive;
	protected final InputStream defInitConfiguration;
	// TODO: we must know its name, because it can be xml, groovy, etc.
	protected final InputStream logbackConfiguration;

	public TrueZIPDeployer(DeploymentSettings settings,
			List<RuntimeComponent> runtimeComponents,
			List<ExtraJavaEEModule> extraModules, InputStream ejbProxyArchive,
			InputStream defInitConfiguration, InputStream logbackConfiguration) {
		this.settings = settings;
		this.runtimeComponents = runtimeComponents;
		this.extraModules = extraModules;
		this.ejbProxyArchive = ejbProxyArchive;
		this.defInitConfiguration = defInitConfiguration;
		this.logbackConfiguration = logbackConfiguration;
	}

	// --- EJBProxyDeployer API ---

	@Override
	public void ejbproxyfy(InputStream sourceEarStream,
			OutputStream targetEarStream) throws Exception {
		// First create a temp file upon all processing will be done.
		File earFile = File.createTempFile(
						"EJBProxy-TrueZIPDeployer-TEMP-EAR-", ".ear");
		TFile earTFile = new TFile(earFile);
		// Copy source ear´s data to the newly created temp file.
		Streams.copy(sourceEarStream, new TFileOutputStream(earTFile));
		TVFS.sync();

		// Create targetPath file
		TFile targetPathTFile = new TFile(earTFile,
				settings.getTargetPath() != null ? settings.getTargetPath()
						.getPath() : "ejbproxy");
		targetPathTFile.mkdir();
		TVFS.sync();

		// Materialize ejbProxyArchive
		TFile ejbProxyArchiveTFile = new TFile(targetPathTFile,
				EJBProxyDeployer.Constants.EJBPROXY_CORE_ARCHIVE_FILE_NAME);
		Streams.copy(ejbProxyArchive, new TFileOutputStream(
				ejbProxyArchiveTFile));
		TVFS.sync();

		// Store custom default initial configuration
		if (defInitConfiguration != null) {
			TFile confTFile = getDefInitConfTFile(ejbProxyArchiveTFile);
			Streams.copy(defInitConfiguration, new TFileOutputStream(confTFile));
		}
		TVFS.sync();

		// Store custom logback configuration
		if (logbackConfiguration != null) {
			TFile logbackTFile = getLogbackTFile(ejbProxyArchiveTFile);
			Streams.copy(logbackConfiguration, new TFileOutputStream(
					logbackTFile));
		}
		TVFS.sync();

		// Get source ear´s modules to active EJBProxy
		List<JavaEEModuleDescriptor> modulesToProcess = getModulesToProcess(earTFile);
		for (JavaEEModuleDescriptor earModule : modulesToProcess) {
			TFile moduleTFile = new TFile(earTFile, earModule.getURI()
					.getPath());

			// Add EJBProxy dependency to modules´ manifests
			TFile moduleMFTFile = getManifestTFile(moduleTFile);
			TFileInputStream moduleMFTFileIS = new TFileInputStream(
					moduleMFTFile);
			Manifest augMF = ManifestUtils.augmentClassPath(new Manifest(
					moduleMFTFileIS), PathUtils.getRelativeToPath(earTFile,
					moduleTFile, ejbProxyArchiveTFile));
			IOUtils.safeClose(moduleMFTFileIS);
			TFileOutputStream moduleMFTFileOS = new TFileOutputStream(
					moduleMFTFile);
			augMF.write(moduleMFTFileOS);
			IOUtils.safeClose(moduleMFTFileOS);

			// Augment ejb deployment descriptors with the EJBProxy default
			TFile ejbDDTFile = getEJBDDTFile(earModule, moduleTFile, true);
			// What about to depend on the ejbproxy-runtime to load the class
			// name from there
			EJBDDUtils.addPrimaryDefaultInterceptor(new TFileInputStream(
					ejbDDTFile), new TFileOutputStream(ejbDDTFile),
					"ejbproxy.runtime.EJBProxyInterceptor");
		}
		TVFS.sync();
		
		// Bundle runtime components
		if (!runtimeComponents.isEmpty()) {
			storeRuntimeComponents(targetPathTFile);
			TFile manifestTFile = getManifestTFile(ejbProxyArchiveTFile);
			TFileInputStream manifestTFileIS = new TFileInputStream(manifestTFile);
			Manifest mf = new Manifest(manifestTFileIS);
			IOUtils.safeClose(manifestTFileIS);

			List<String> rCompNames = new ArrayList<String>(
					runtimeComponents.size());
			for (RuntimeComponent rComp : runtimeComponents) {
				rCompNames.add(rComp.getName());
			}
			ManifestUtils.augmentClassPath(mf, false,
					rCompNames.toArray(new String[rCompNames.size()]));
			TFileOutputStream manifestTFileOS = new TFileOutputStream(manifestTFile);
			mf.write(manifestTFileOS);
			IOUtils.safeClose(manifestTFileOS);
		}
		TVFS.sync();

		// Bundle extra modules{
		if (!extraModules.isEmpty()) {
			storeExtraModules(earTFile);
			if (settings.getAddExtraModulesToDeploymentDescriptor() == null
					|| settings.getAddExtraModulesToDeploymentDescriptor()) {
				TFile javaEEDD = getJavaEEDDTFile(earTFile, true);
				JavaEEDDUtils.addExtraModules(new TFileInputStream(javaEEDD),
						new TFileOutputStream(javaEEDD), extraModules);
			}
		}
		TVFS.umount();

		Streams.copy(new FileInputStream(earFile), targetEarStream);
		if (!earFile.delete()) {
			log.warn("Created temp files could not be deleted.");
		}
	}

	// --- Utilities ---

	private void storeExtraModules(TFile folder) throws IOException {
		for (ExtraJavaEEModule extraModule : extraModules) {
			TFile eModuleTFile = new TFile(folder, extraModule.getDescriptor()
					.getURI().getPath());
			eModuleTFile.createNewFile();
			InputStream eModuleArchive = extraModule.getArchive();
			eModuleTFile.input(eModuleArchive);
			IOUtils.safeClose(eModuleArchive);
		}
	}

	// What about to depend on the ejbproxy-runtime to load the path from there
	private TFile getLogbackTFile(TFile root) {
		return new TFile(root,
				"ejbproxy/runtime/logging/logback.xml");
	}

	// What about to depend on the ejbproxy-runtime to load the path from there
	private TFile getDefInitConfTFile(TFile root) {
		return new TFile(
				root,
				"ejbproxy/runtime/configuration/initial/ejbproxy-initconfig.properties");
	}

	private TFile getManifestTFile(TFile archive) throws IOException {
		TFile mfTFile = new TFile(new TFile(archive, "META-INF"), "MANIFEST.MF");
		if (!mfTFile.exists()) {
			mfTFile.createNewFile();
			TVFS.sync();
		}
		return mfTFile;
	}

	private TFile getJavaEEDDTFile(TFile archive, boolean create)
			throws IOException {
		TFile mfTFile = new TFile(new TFile(archive, "META-INF"),
				"application.xml");
		if (create && !mfTFile.exists()) {
			mfTFile.createNewFile();
			JavaEEDDUtils.writeDDStub(new TFileOutputStream(mfTFile));
		}
		return mfTFile;
	}

	private TFile getEJBDDTFile(JavaEEModuleDescriptor earModule,
			TFile archive, boolean create) throws IOException {
		TFile dd = null;
		if (Type.EJB == earModule.getType()) {
			dd = new TFile(new TFile(archive, "META-INF"), "ejb-jar.xml");
			if (create && !dd.exists()) {
				dd.createNewFile();
				EJBDDUtils.writeDDStub(new TFileOutputStream(dd), "0");
			}
		} else if (Type.WEB == earModule.getType()) {
			dd = new TFile(new TFile(archive, "WEB-INF"), "ejb-jar.xml");
			if (create && !dd.exists()) {
				dd.createNewFile();
				EJBDDUtils.writeDDStub(new TFileOutputStream(dd), "1");
			}
		}
		return dd;
	}

	private void storeRuntimeComponents(TFile folder) throws IOException {
		for (RuntimeComponent rComponent : runtimeComponents) {
			TFile rComponentTFile = new TFile(folder, rComponent.getName());
			InputStream rCompArchive = rComponent.getArchive();
			rComponentTFile.input(rCompArchive);
			IOUtils.safeClose(rCompArchive);
		}
	}

	private List<JavaEEModuleDescriptor> getModulesToProcess(TFile ear)
			throws Exception {
		if (settings.getModulesToProcess() != null) {
			return settings.getModulesToProcess();
		} else {
			TFile dd = getJavaEEDDTFile(ear, false);
			if (dd.isFile()) {
				List<JavaEEModuleDescriptor> ddModules = JavaEEDDUtils
						.getSpecifiedEjbAndWebModules(new TFileInputStream(dd));
				if (settings.getModulesToSkip() != null) {
					ddModules.removeAll(settings.getModulesToSkip());
				}
				return ddModules;
			} else {
				log.warn("JavaEE deployment descriptor does not exist and no explicit modules were specified. No module will be processed.");
				return Collections.emptyList();
			}
		}
	}
}
