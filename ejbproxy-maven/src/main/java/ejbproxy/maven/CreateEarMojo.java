package ejbproxy.maven;

import ejbproxy.deploy.EJBProxyDeployer;
import ejbproxy.deploy.EJBProxyDeployerBuilder;
import ejbproxy.deploy.ExtraJavaEEModule;
import ejbproxy.deploy.JavaEEModuleDescriptor;
import ejbproxy.deploy.JavaEEModuleDescriptor.Type;
import ejbproxy.deploy.RuntimeComponent;
import ejbproxy.deploy.WebJavaEEModuleDescriptor;
import ejbproxy.deploy.util.FileExtraJavaEEModule;
import ejbproxy.deploy.util.FileRuntimeComponent;
import ejbproxy.maven.util.FileLister;
import ejbproxy.maven.util.PrefixFileFilter;
import ejbproxy.maven.util.SuffixFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates new 'ejbproxyfied' ear from a source ear.
 * 
 * @goal create-ear
 * @requiresProject true
 * @phase package
 */
public class CreateEarMojo extends AbstractMojo {
	private static final String CORE_RUNTIME_ARCHIVE_FILENAME_PREFIX = "ejbproxy-runtime";
	private static final String CORE_RUNTIME_ARCHIVE_FILENAME_SUFFIX = ".jar";
	private static final String DEF_INIT_CONFIG_FILENAME_PREFIX = "ejbproxy-initconfig";
	private static final String DEF_INIT_CONFIG_FILENAME_SUFFIX = ".properties";
	private static final String LOGBACK_CONFIG_FILENAME_PREFIX = "logback";
	private static final String RUNTIME_COMPONENTS_DIRECTORY_NAME = "runtime-components";
	private static final String EXTRA_MODULES_DIRECTORY_NAME = "extra-modules";

	/**
	 * @parameter expression="${project.build.ejbproxySourceDirectory}"
	 *            default-value="${project.basedir}/src/ejbproxy"
	 */
	private File ejbProxySourceDirectory;

	/**
	 * @parameter expression="${project.build.ejbproxyTargetDirectory}"
	 *            default-value="${project.build.directory}/ejbproxy"
	 */
	private File ejbProxyTargetDirectory;

	/**
	 * @parameter expression="${project.build.directory}"
	 */
	private File buildDirectory;

	/**
	 * @parameter expression="${project.build.finalName}.${project.packaging}"
	 */
	private String earName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		checkInvariants();
		createTargetFolder();

		// builder creation
		EJBProxyDeployerBuilder builder = new EJBProxyDeployerBuilder();

		File coreRuntimeArchiveFile = getFirstPassed(
				CORE_RUNTIME_ARCHIVE_FILENAME_PREFIX,
				CORE_RUNTIME_ARCHIVE_FILENAME_SUFFIX, ejbProxySourceDirectory,
				0);
		builder.setEjbProxyArchive(getInputStream(coreRuntimeArchiveFile,
				"No EJBProxy core runtime archive was specified."));

		File defInitConfigurationFile = getFirstPassed(
				DEF_INIT_CONFIG_FILENAME_PREFIX,
				DEF_INIT_CONFIG_FILENAME_SUFFIX, ejbProxySourceDirectory, 0);
		builder.setDefInitConfiguration(getInputStream(
				defInitConfigurationFile, null));

		File logbackConfigurationFile = getFirstPassed(
				LOGBACK_CONFIG_FILENAME_PREFIX, null, ejbProxySourceDirectory,
				0);
		builder.setLogbackConfiguration(getInputStream(
				logbackConfigurationFile, null));

		builder.setRuntimeComponents(createRuntimeComponents(new File(
				ejbProxySourceDirectory, RUNTIME_COMPONENTS_DIRECTORY_NAME)));

		builder.setExtraModules(createExtraModules(new File(
				ejbProxySourceDirectory, EXTRA_MODULES_DIRECTORY_NAME)));

		// ear creation
		try {
			EJBProxyDeployer deployer = builder.buildTrueZIPDeployer();
			logDeployerSettings(getLog(), coreRuntimeArchiveFile,
					defInitConfigurationFile, logbackConfigurationFile,
					builder.getRuntimeComponents(), builder.getExtraModules());

			deployer.ejbproxyfy(new FileInputStream(new File(buildDirectory,
					earName)), new FileOutputStream(new File(
					ejbProxyTargetDirectory, earName)));
			getLog().info(
					String.format("Ear '%s' successfully created.", new File(
							ejbProxyTargetDirectory, earName).getAbsolutePath()));
		} catch (Exception e) {
			StringWriter stackTraceHolder = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTraceHolder));
			getLog().debug(stackTraceHolder.toString());
			throw new MojoFailureException(
					"Resulting 'ejbproxyfied' ear could not be created! An unexpected exception occurred: "
							+ e);
		}
	}

	// --- Utilities ---

	private void checkInvariants() throws MojoFailureException {
		if (ejbProxySourceDirectory == null
				|| !ejbProxySourceDirectory.isDirectory()) {
			throw new MojoFailureException(
					String.format(
							"Illegal 'ejbproxySourceDirectory' parameter specified: %s",
							ejbProxySourceDirectory));
		}
		if (ejbProxyTargetDirectory == null
				|| (ejbProxyTargetDirectory.exists() && !ejbProxyTargetDirectory
						.isDirectory())) {
			throw new MojoFailureException(
					String.format(
							"Illegal 'ejbProxyTargetDirectory' parameter specified: %s",
							ejbProxyTargetDirectory));
		}
	}

	private void createTargetFolder() throws MojoFailureException {
		if (!ejbProxyTargetDirectory.exists()) {
			if (!ejbProxyTargetDirectory.mkdirs()) {
				throw new MojoFailureException(String.format(
						"Could not create 'ejbProxyTargetDirectory': %s",
						ejbProxyTargetDirectory));
			}
		}
	}

	private List<RuntimeComponent> createRuntimeComponents(File rootDir) {
		if (!rootDir.isDirectory()) {
			return null;
		} else {
			List<RuntimeComponent> components = new ArrayList<RuntimeComponent>();
			for (File compFile : new FileLister(rootDir, new SuffixFileFilter(
					".jar")).listFiles()) {
				components.add(new FileRuntimeComponent(compFile.getName(),
						compFile));
			}
			return components;
		}
	}

	private List<ExtraJavaEEModule> createExtraModules(File rootDir) {
		if (!rootDir.isDirectory()) {
			return null;
		} else {
			List<ExtraJavaEEModule> modules = new ArrayList<ExtraJavaEEModule>();
			if (new File(rootDir, "ejb").isDirectory()) {
				File ejbModsRoot = new File(rootDir, "ejb");
				URI ejbModsRootUri = ejbModsRoot.toURI();
				for (File ejbModFile : new FileLister(ejbModsRoot,
						new SuffixFileFilter(".jar")).listFiles()) {
					modules.add(new FileExtraJavaEEModule(
							new JavaEEModuleDescriptor(Type.EJB, ejbModsRootUri
									.relativize(ejbModFile.toURI())),
							ejbModFile));
				}
			}
			if (new File(rootDir, "web").isDirectory()) {
				File webModsRoot = new File(rootDir, "web");
				URI webModsRootUri = webModsRoot.toURI();
				for (File webModFile : new FileLister(webModsRoot,
						new SuffixFileFilter(".war")).listFiles()) {
					modules.add(new FileExtraJavaEEModule(
							new WebJavaEEModuleDescriptor(webModsRootUri
									.relativize(webModFile.toURI()), "/"
									+ webModFile.getName().substring(0,
											webModFile.getName().length() - 4)),
							webModFile));
				}
			}
			if (new File(rootDir, "java").isDirectory()) {
				File javaModsRoot = new File(rootDir, "java");
				URI javaModsRootUri = javaModsRoot.toURI();
				for (File javaModFile : new FileLister(javaModsRoot,
						new SuffixFileFilter(".jar")).listFiles()) {
					modules.add(new FileExtraJavaEEModule(
							new JavaEEModuleDescriptor(Type.JAVA,
									javaModsRootUri.relativize(javaModFile
											.toURI())), javaModFile));
				}
			}
			if (new File(rootDir, "connector").isDirectory()) {
				File connectorModsRoot = new File(rootDir, "connector");
				URI connectorModsRootUri = connectorModsRoot.toURI();
				for (File connectorModFile : new FileLister(connectorModsRoot,
						new SuffixFileFilter(".rar")).listFiles()) {
					modules.add(new FileExtraJavaEEModule(
							new JavaEEModuleDescriptor(Type.CONNECTOR,
									connectorModsRootUri
											.relativize(connectorModFile
													.toURI())),
							connectorModFile));
				}
			}
			return modules;
		}
	}

	private File getFirstPassed(String prefix, String suffix, File root,
			int maxDepth) {
		List<File> passedFiles = new ArrayList<File>();
		if (prefix != null) {
			FileFilter prefixFilter = new PrefixFileFilter(prefix);
			passedFiles.addAll(new FileLister(root, maxDepth, prefixFilter)
					.listFiles());
		}
		if (suffix != null) {
			FileFilter suffixFilter = new SuffixFileFilter(suffix);
			if (prefix != null) {
				passedFiles.retainAll(new FileLister(root, maxDepth,
						suffixFilter).listFiles());
			} else {
				passedFiles.addAll(new FileLister(root, maxDepth, suffixFilter)
						.listFiles());
			}
		}
		return passedFiles.isEmpty() ? null : passedFiles.get(0);
	}

	private InputStream getInputStream(File file, String errMsg)
			throws MojoFailureException {
		try {
			return new FileInputStream(file);
		} catch (Exception e) {
			if (errMsg != null) {
				throw new MojoFailureException(String.format(errMsg, e));
			} else {
				return null;
			}
		}
	}

	private static void logDeployerSettings(Log log,
			File coreRuntimeArchiveFile, File defInitConfigurationFile,
			File logbackConfigurationFile,
			List<RuntimeComponent> runtimeComponents,
			List<ExtraJavaEEModule> extraModules) {
		StringBuilder logMsg = new StringBuilder();

		logMsg.append("Creating ear with these settings:\n");

		logMsg.append(String.format("Core runtime archive:\n - %s",
				coreRuntimeArchiveFile.getName()));

		if (defInitConfigurationFile != null) {
			logMsg.append(String.format(
					"\nDefault initial configuration:\n - %s",
					defInitConfigurationFile.getName()));
		}
		if (logbackConfigurationFile != null) {
			logMsg.append(String.format("\nLogback configuration:\n - %s",
					logbackConfigurationFile.getName()));
		}

		if (runtimeComponents != null && !runtimeComponents.isEmpty()) {
			logMsg.append("\nRuntime components:");
			for (RuntimeComponent runtimeComponent : runtimeComponents) {
				logMsg.append(String.format("\n - %s",
						runtimeComponent.getName()));
			}
		}
		if (extraModules != null && !extraModules.isEmpty()) {
			logMsg.append("\nExtra modules:");
			for (ExtraJavaEEModule extraModule : extraModules) {
				logMsg.append(String.format("\n - %s (%s)", extraModule
						.getDescriptor().getURI(), extraModule.getDescriptor()
						.getType()));
			}
		}

		log.info(logMsg.toString());
	}

	public static void main(String[] args) throws Exception {
		new CreateEarMojo().execute();
	}
}
