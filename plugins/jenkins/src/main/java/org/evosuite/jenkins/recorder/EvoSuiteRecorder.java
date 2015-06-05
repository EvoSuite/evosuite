package org.evosuite.jenkins.recorder;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import org.evosuite.jenkins.actions.BuildAction;
import org.evosuite.jenkins.actions.ProjectAction;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class EvoSuiteRecorder extends Recorder {

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public EvoSuiteRecorder() {
		// empty
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		if (project.getLastBuild() != null) {
			ProjectAction lastProject = project.getLastBuild().getAction(BuildAction.class).getProjectAction();
			return new ProjectAction(project, lastProject.getModules());
		}

		return new ProjectAction(project);
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

		AbstractMavenProject<?, ?> project = ((AbstractMavenProject<?, ?>) build.getProject());
		ProjectAction projectAction = new ProjectAction(project);
		if (!projectAction.perform(project, build)) {
			return false;
		}

		BuildAction build_action = new BuildAction(build, projectAction);
		build.addAction(build_action);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.Recorder#getDescriptor()
	 */
	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super(EvoSuiteRecorder.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return "Add EvoSuite Stats";
		}

		public FormValidation doCheckCreateStats(@QueryParameter Boolean value) throws IOException, ServletException {
			if (value == false) {
				return FormValidation.error("Stats must be created for any infomation to be displayed");
			} else {
				return FormValidation.ok();
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		 */
		@Override
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return Boolean.TRUE;
		}
	}
}
