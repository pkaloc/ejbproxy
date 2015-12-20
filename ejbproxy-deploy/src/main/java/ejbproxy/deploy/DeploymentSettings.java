package ejbproxy.deploy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * EJBProxy deployment is parameterized. This parameterization can be customized
 * with the aid of this class.
 */
public class DeploymentSettings {
	protected List<JavaEEModuleDescriptor> modulesToSkip;
	protected List<JavaEEModuleDescriptor> modulesToProcess;
	protected URI targetPath;
	protected Boolean addExtraModulesToDeploymentDescriptor;

	/**
	 * By default, EAR modules for which the EJBProxy will be activated are read
	 * from the ear´s deployment descriptor (<code>application.xml</code> file).
	 * With the aid of this method, the deployer can request to skip some of the
	 * found modules.
	 * <p>
	 * This method may return <code>null</code>, which is also the default
	 * value. That is, no <code>application.xml</code> module is skipped by
	 * default.
	 */
	public List<JavaEEModuleDescriptor> getModulesToSkip() {
		return modulesToSkip;
	}

	/**
	 * If not <code>null</code> (the default), modules for which to activate the
	 * EJBProxy are not automatically discovered by reading the ear´s deployment
	 * descriptor, but solely specified by this method. That is, the deployment
	 * descriptor is skipped.
	 */
	public List<JavaEEModuleDescriptor> getModulesToProcess() {
		return modulesToProcess;
	}

	/**
	 * The relative path where the resulting EJBProxy archive and all its
	 * (possible) runtime dependencies will be stored within the ear.
	 * <p>
	 * If <code>null</code>, all resulting artifacts will be placed inside the '
	 * <code>ejbproxy</code>' folder.
	 */
	public URI getTargetPath() {
		return targetPath;
	}

	/**
	 * If <code>true</code>, all specified extra modules will be added into the
	 * ear´s deployment descriptor. The DD will also be created if does not
	 * exist yet.
	 * <p>
	 * If <code>null</code>, all specified extra modules will be added into the
	 * ear´s deployment descriptor by default.
	 */
	public Boolean getAddExtraModulesToDeploymentDescriptor() {
		return addExtraModulesToDeploymentDescriptor;
	}

	public DeploymentSettings setAddExtraModulesToDeploymentDescriptor(
			Boolean addExtraModulesToDeploymentDescriptor) {
		this.addExtraModulesToDeploymentDescriptor = addExtraModulesToDeploymentDescriptor;
		return this;
	}

	public DeploymentSettings setTargetPath(URI targetPath) {
		this.targetPath = targetPath;
		return this;
	}

	public DeploymentSettings setModulesToSkip(
			List<JavaEEModuleDescriptor> modulesToSkip) {
		this.modulesToSkip = modulesToSkip;
		return this;
	}

	public DeploymentSettings setModulesToProcess(
			List<JavaEEModuleDescriptor> modulesToProcess) {
		this.modulesToProcess = modulesToProcess;
		return this;
	}

	public DeploymentSettings addModuleToSkip(JavaEEModuleDescriptor module) {
		if (modulesToSkip == null) {
			modulesToSkip = new ArrayList<JavaEEModuleDescriptor>();
		}
		modulesToSkip.add(module);
		return this;
	}

	public DeploymentSettings addModuleToProcess(JavaEEModuleDescriptor module) {
		if (modulesToProcess == null) {
			modulesToProcess = new ArrayList<JavaEEModuleDescriptor>();
		}
		modulesToProcess.add(module);
		return this;
	}
}
