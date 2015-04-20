package org.evosuite.jenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.ServletException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 */
public class EvosuiteRecorder extends Recorder {

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	private final boolean displayDefaultProjectInfo;
	private final boolean displayBuildInfo;
	private final boolean displayBuildSummary;
	private final boolean displayProjectInfo;
	private FilePath workspacedir;

	@DataBoundConstructor
//	public EvosuiteRecorder(Boolean create, Boolean defaultProjectInfo, Boolean projectInfo, Boolean buildSummary, Boolean buildInfo) {
	public EvosuiteRecorder(Boolean defaultProjectInfo, Boolean projectInfo, Boolean buildSummary, Boolean buildInfo) {
		this.displayDefaultProjectInfo = defaultProjectInfo;
		this.displayBuildInfo = buildInfo;
		this.displayProjectInfo = projectInfo;
		this.displayBuildSummary = buildSummary;
	}

	public FilePath getWorkspaceDir() {
		return workspacedir;
	}

	public boolean getDisplayProjectInfo() {
		return displayProjectInfo;
	}

	public boolean getDisplayBuildInfo() {
		return displayBuildInfo;
	}

	public boolean getDisplayBuildSummary() {
		return displayBuildSummary;
	}

	public boolean getDisplayDefaultProjectInfo() {
		return displayDefaultProjectInfo;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */

	 public BuildStepMonitor getRequiredMonitorService() {
		 return BuildStepMonitor.NONE;
	 }
	 //	private boolean hasEvosuiteFile(Path p){
	 //            return true;
	 //        }
	 /* (non-Javadoc)
	  * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
	  */

	 @Override
	 public Action getProjectAction(AbstractProject<?, ?> project) {
		 return new EvosuiteProjectAction(project, this);
	 }

	 @Override
	 public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			 BuildListener listener) throws InterruptedException, IOException {
		 listener.getLogger().append("Creating Evosuite Statistics\n");
		 AbstractMavenProject<?, ?> prj = ((AbstractMavenProject<?, ?>) build
				 .getProject());
		 MavenModuleSet mms = (MavenModuleSet) prj;
		 //MavenModuleSetBuild mmsb = (MavenModuleSetBuild) build;
		 Collection<MavenModule> cmodule = mms.getModules();
		 Collection<MavenModule> validmodules = new ArrayList<MavenModule>();
		 Iterator<MavenModule> iterator = cmodule.iterator();
		 workspacedir = build.getWorkspace();
		 listener.getLogger().append("Local Workspace Directory : "+ workspacedir.getRemote()+ "\n");

		 while (iterator.hasNext()) {
			 //                check if the module has evosuite project info
			 MavenModule asdf = (MavenModule) iterator.next();

			 Path path = Paths.get(workspacedir.getRemote() + // File.separator + asdf.getModuleName().artifactId + 
					 File.separator +".continuous_evosuite" + File.separator + "project_info.xml");
			 if (Files.exists(path)) {
				 validmodules.add(asdf);
			 } else {
				 listener.getLogger().append("Evosuite files not found, skipping the " + asdf.getModuleName() + " Module\n");
			 }
		 }
		 Iterator<MavenModule> mit = validmodules.iterator();
		 while (mit.hasNext()) {
			 MavenModule locatedmodule = (MavenModule) mit.next();
			 listener.getLogger().append("Using Evosuite files for: " + locatedmodule.toString()+ "\n");
		 }
		 build.addAction(new EvosuiteBuildAction(validmodules, build, this));

		 return true;
	 }

	 /* (non-Javadoc)
	  * @see hudson.tasks.Recorder#getDescriptor()
	  */
	 @SuppressWarnings({"rawtypes"})
	 @Override
	 public BuildStepDescriptor getDescriptor() {
		 return DESCRIPTOR;
	 }

	 public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		 public DescriptorImpl() {
			 super(EvosuiteRecorder.class);
		 }

		 /* (non-Javadoc)
		  * @see hudson.model.Descriptor#getDisplayName()
		  */
		 @Override
		 public String getDisplayName() {
			 return "Add Evosuite Stats";
		 }

		 public FormValidation doCheckCreateStats(@QueryParameter Boolean value) throws IOException, ServletException {
			 if (value == false) {
				 return FormValidation.error("Stats must be created for any infomation to be displayed");
			 } else {
				 return FormValidation.ok();
			 }

		 }
		 /* (non-Javadoc)
		  * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		  */

		 @Override
		 public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			 return Boolean.TRUE;
		 }
	 }
}
