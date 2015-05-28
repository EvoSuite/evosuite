package org.evosuite.jenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSetList;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialChangeSet;
import hudson.plugins.mercurial.MercurialChangeSetList;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.ChangeLogParser;
import hudson.scm.EditType;
import hudson.scm.PollingResult;
import hudson.scm.SCMRevisionState;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.SCM;
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
import org.xml.sax.SAXException;

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

		 MavenModuleSet p = (MavenModuleSet) build.getProject();
		 SCM scm = p.getScm();

		 // hudson.plugins.mercurial.MercurialSCM
		 if (scm.getType().equals(MercurialSCM.class.getCanonicalName())) {
			 System.out.println("eheheh I'm using Mercurial @ " + build.getWorkspace());

			 for (ChangeLogSet<? extends Entry> change : build.getChangeSets()) {
				 System.out.println("change: " + change.toString());

				 for (MercurialChangeSet changeset : ((MercurialChangeSetList) change)) {
					 System.out.println(changeset.getAddedPaths());
					 System.out.println(changeset.getDeletedPaths());
					 System.out.println(changeset.getModifiedPaths());
				 }
			 }
		 }
		 else if (scm.getType().equals(GitSCM.class.getCanonicalName()))
		 {
			 System.out.println("eheheh I'm using Git @ " + build.getWorkspace());

			 for (ChangeLogSet<? extends Entry> change : build.getChangeSets()) {
				 System.out.println("change: " + change.toString());

				 for (GitChangeSet changeset : ((GitChangeSetList) change)) {
					 for (hudson.plugins.git.GitChangeSet.Path path : changeset.getPaths()) {
						 switch (path.getEditType().getName()) {
						 	case "add":
						 		System.out.println("A " + path.getPath());
						 		break;
						 	case "delete":
						 		System.out.println("D " + path.getPath());
						 		break;
						 	case "edit":
						 		System.out.println("M " + path.getPath());
						 		break;
						 	default:
						 		break;
						 }
					 }
				 }
			 }
		 }
		 else {
			 System.err.println("SCM of type " + scm.getType() + " not supported");
		 }
		 
		 
		 
		 
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
	 @Override
	 public BuildStepDescriptor<Publisher> getDescriptor() {
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
