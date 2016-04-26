/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.jenkins.recorder;

import org.evosuite.jenkins.actions.BuildAction;
import org.evosuite.jenkins.actions.ProjectAction;
import org.evosuite.jenkins.scm.Git;
import org.evosuite.jenkins.scm.Mercurial;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;

public class EvoSuiteRecorder extends Recorder {

	public static final String LOG_PREFIX = "[EvoSuite] ";

	public static final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	private boolean disableAutoCommit;
	private boolean disableAutoPush;
	private String branchName;
	private String ctgBestsDir;

	@DataBoundConstructor
	public EvoSuiteRecorder(boolean disableAutoCommit, boolean disableAutoPush,
	    String branchName, String ctgBestsDir) {
		this.disableAutoCommit = disableAutoCommit;
		this.disableAutoPush = disableAutoPush;
		this.branchName = branchName;
		this.ctgBestsDir = ctgBestsDir;
	}

	public boolean getDisableAutoCommit() {
		return this.disableAutoCommit;
	}

	public boolean getDisableAutoPush() {
		return this.disableAutoPush;
	}

	public String getBranchName() {
		return this.branchName;
	}

	public String getCtgBestsDir() {
	    return this.ctgBestsDir;
	}

	public void setDisableAutoCommit(boolean disableAutoCommit) {
		this.disableAutoCommit = disableAutoCommit;
	}

	public void setDisableAutoPush(boolean disableAutoPush) {
		this.disableAutoPush = disableAutoPush;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public void setCtgBestsDir(String ctgBestsDir) {
        this.ctgBestsDir = ctgBestsDir;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		if (!project.getBuilds().isEmpty() /*|| !project.getActions().isEmpty()*/) {
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
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Build did not succeed, so no test case generation by EvoSuite will occur.");
			return true;
		}

		AbstractMavenProject<?, ?> project = ((AbstractMavenProject<?, ?>) build.getProject());
		ProjectAction projectAction = new ProjectAction(project);
		projectAction.perform(project, build, listener);

		BuildAction build_action = new BuildAction(build, projectAction);
		build.addAction(build_action);

		// Deliver new test cases (i.e., commit and push the new test cases generated)

		if (this.disableAutoCommit) {
			return true;
		}

		SCM scm = project.getScm();
		if (scm == null) {
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Project '" + project.getName() + "' has no Source-Control-Management (SCM) defined.");
			return true;
		}

		org.evosuite.jenkins.scm.SCM scmWrapper = null;

		if (scm instanceof MercurialSCM) {
			scmWrapper = new Mercurial((MercurialSCM) scm, project, build, launcher, listener);
		} else if (scm instanceof GitSCM) {
			scmWrapper = new Git((GitSCM) scm, build, listener);
		} else {
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "SCM of type " + scm.getType() + " not supported!");
			return true;
		}
		assert scmWrapper != null;

		// perform commit action
		int number_of_files_committed = scmWrapper.commit(project, build, listener, this.branchName, this.ctgBestsDir);
		if (number_of_files_committed == -1) {
			return false;
		}

		if (!this.disableAutoPush && number_of_files_committed > 0) {
			if (!scmWrapper.push(build, listener, this.branchName)) {
				return false;
			}
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
			return "EvoSuite";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		 */
		@Override
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
		 */
		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			return (EvoSuiteRecorder) super.newInstance(req, formData);
		}
	}
}
