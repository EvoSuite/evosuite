/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.jenkins.recorder;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import org.evosuite.jenkins.actions.BuildAction;
import org.evosuite.jenkins.actions.ProjectAction;
import org.evosuite.jenkins.scm.Mercurial;
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
		if (!project.getBuilds().isEmpty() || !project.getActions().isEmpty()) {
			BuildAction buildAction = project.getLastBuild().getAction(BuildAction.class);
			if (buildAction != null) {
				ProjectAction lastProject = buildAction.getProjectAction();
				return new ProjectAction(project, lastProject.getModules());
			}
		}

		return new ProjectAction(project);
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

		if (build.getResult().isWorseThan(Result.SUCCESS)) {
			listener.getLogger().println("Build did not succeed, so no test case generation by EvoSuite will occur.");
			return true;
		}

		AbstractMavenProject<?, ?> project = ((AbstractMavenProject<?, ?>) build.getProject());
		ProjectAction projectAction = new ProjectAction(project);
		if (!projectAction.perform(project, build)) {
			return false;
		}

		BuildAction build_action = new BuildAction(build, projectAction);
		build.addAction(build_action);

		// FIXME the new test cases generated improved the coverage of manual written test cases?
		// maybe we should do this on evosuite-maven-plugin?


		// Deliver new test cases (i.e., commit and push the new test cases generated)

		SCM scm = project.getScm();
		if (scm == null) {
			listener.getLogger().println("Project '" + project.getName() + "' has no Source-Control-Management (SCM) defined.");
			return true;
		}

		if (scm instanceof MercurialSCM) {
			Mercurial m = new Mercurial((MercurialSCM) scm, project);
			if (!m.commit(build, launcher, listener)) {
				return false;
			}
			if (!m.push(build, launcher, listener)) {
				return false;
			}
		}
		else if (scm instanceof GitSCM) {
			// empty
		}
		else if (scm instanceof SubversionSCM) {
			// empty
		}
		else {
			listener.getLogger().println("SCM of type " + scm.getType() + " not supported!");
		}

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
