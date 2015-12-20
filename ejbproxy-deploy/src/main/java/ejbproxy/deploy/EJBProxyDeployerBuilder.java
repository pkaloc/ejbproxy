package ejbproxy.deploy;

import ejbproxy.deploy.impl.truezip.TrueZIPDeployer;
import org.apache.commons.lang3.Validate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// TODO: comments, refactorings, etc.
// All provided streams are closed
public class EJBProxyDeployerBuilder {
	protected DeploymentSettings settings;
	protected List<RuntimeComponent> runtimeComponents;
	protected List<ExtraJavaEEModule> extraModules;

	protected InputStream ejbProxyArchive;
	protected InputStream defInitConfiguration;
	protected InputStream logbackConfiguration;

	public EJBProxyDeployer buildTrueZIPDeployer() {
		Validate.notNull(ejbProxyArchive);

		return new TrueZIPDeployer(settings == null ? new DeploymentSettings()
				: settings,
				runtimeComponents == null ? new ArrayList<RuntimeComponent>()
						: runtimeComponents,
				extraModules == null ? new ArrayList<ExtraJavaEEModule>()
						: extraModules, ejbProxyArchive, defInitConfiguration,
				logbackConfiguration);
	}

	public DeploymentSettings getSettings() {
		return settings;
	}

	public EJBProxyDeployerBuilder setSettings(DeploymentSettings settings) {
		this.settings = settings;
		return this;
	}

	public List<RuntimeComponent> getRuntimeComponents() {
		return runtimeComponents;
	}

	public EJBProxyDeployerBuilder setRuntimeComponents(
			List<RuntimeComponent> runtimeComponents) {
		this.runtimeComponents = runtimeComponents;
		return this;
	}

	public EJBProxyDeployerBuilder addRuntimeComponent(
			RuntimeComponent runtimeComponent) {
		if (runtimeComponents == null) {
			runtimeComponents = new ArrayList<RuntimeComponent>();
		}
		runtimeComponents.add(runtimeComponent);
		return this;
	}

	public List<ExtraJavaEEModule> getExtraModules() {
		return extraModules;
	}

	public EJBProxyDeployerBuilder setExtraModules(
			List<ExtraJavaEEModule> extraModules) {
		this.extraModules = extraModules;
		return this;
	}

	public EJBProxyDeployerBuilder addExtraModule(
			ExtraJavaEEModule extraJavaEEModule) {
		if (extraModules == null) {
			extraModules = new ArrayList<ExtraJavaEEModule>();
		}
		extraModules.add(extraJavaEEModule);
		return this;
	}

	public InputStream getEjbProxyArchive() {
		return ejbProxyArchive;
	}

	public EJBProxyDeployerBuilder setEjbProxyArchive(
			InputStream ejbProxyArchive) {
		this.ejbProxyArchive = Validate.notNull(ejbProxyArchive);
		return this;
	}

	public InputStream getDefInitConfiguration() {
		return defInitConfiguration;
	}

	public EJBProxyDeployerBuilder setDefInitConfiguration(
			InputStream defInitConfiguration) {
		this.defInitConfiguration = defInitConfiguration;
		return this;
	}

	public InputStream getLogbackConfiguration() {
		return logbackConfiguration;
	}

	public EJBProxyDeployerBuilder setLogbackConfiguration(
			InputStream logbackConfiguration) {
		this.logbackConfiguration = logbackConfiguration;
		return this;
	}
}