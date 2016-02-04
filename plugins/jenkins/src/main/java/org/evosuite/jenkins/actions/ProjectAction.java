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
package org.evosuite.jenkins.actions;

import org.evosuite.Properties;
import org.evosuite.jenkins.plot.CoveragePlot;
import org.evosuite.jenkins.plot.TimePlot;
import org.evosuite.jenkins.recorder.EvoSuiteRecorder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;

public class ProjectAction implements Action {

	private final AbstractProject<?, ?> project;
	private List<ModuleAction> modules = new ArrayList<ModuleAction>();

	public ProjectAction(AbstractProject<?, ?> project) {
		this.project = (AbstractProject<?, ?>) project;
	}

	public ProjectAction(AbstractProject<?, ?> project, List<ModuleAction> modules) {
		this.project = (AbstractProject<?, ?>) project;
		this.modules.addAll(modules);
	}

	@Override
	public String getIconFileName() {
		return "/plugin/evosuite-jenkins-plugin/icons/evosuite.png";
	}

	@Override
	public String getDisplayName() {
		return "EvoSuite Project Statistics";
	}

	@Override
	public String getUrlName() {
		return "evosuite-project";
	}

	public AbstractProject<?, ?> getProject() {
		return this.project;
	}

	public String getName() {
		return this.project.getName();
	}

	public List<ModuleAction> getModules() {
		return this.modules;
	}

	private void saveTests(AbstractBuild<?, ?> build, BuildListener listener,
			String moduleName) throws InterruptedException, IOException {

		FilePath workspace = build.getWorkspace();

		// FIXME should we also use module.getRelativePath() ?!
		FilePath[] testsGenerated = workspace.list(build.getEnvironment(listener).expand(
				Properties.CTG_DIR + File.separator + "tmp_*" + File.separator +
				Properties.CTG_TMP_TESTS_DIR_NAME + File.separator + "**" + File.separator + "*"));
		for (FilePath testGenerated : testsGenerated) {
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "From_testsGenerated: " + testGenerated.getRemote());

			FilePath to = new FilePath(new File(
					testGenerated.getRemote().replace(workspace.getRemote(),
							build.getRootDir().getAbsolutePath() + File.separator + ".." + File.separator + moduleName + File.separator)));
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "To_testsGenerated: " + to.getRemote());
			testGenerated.copyTo(to);
		}
	}

	private File saveProjectInfoXml(AbstractBuild<?, ?> build, BuildListener listener,
			String moduleName) throws InterruptedException, IOException {

		// FIXME should we also use module.getRelativePath() ?!
		// FIXME check if this code does not return a null
		FilePath from = build.getWorkspace().list(build.getEnvironment(listener).expand(Properties.CTG_DIR + File.separator + Properties.CTG_PROJECT_INFO))[0];
		FilePath to = new FilePath(new File(build.getRootDir(),
				File.separator + moduleName + File.separator + Properties.CTG_DIR + File.separator + Properties.CTG_PROJECT_INFO));
		listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "From: " + from.getRemote());
		listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "To: " + to.getRemote());

		from.copyTo(to);
		return new File(to.getRemote());
	}

	public boolean perform(AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build,
			BuildListener listener) throws InterruptedException, IOException {

		EnvVars env = build.getEnvironment(listener);
		env.overrideAll(build.getBuildVariables());

		MavenModuleSet prj = (MavenModuleSet) this.project;
		for (MavenModule module : prj.getModules()) {
			this.saveTests(build, listener, module.getName());
			File project_info = this.saveProjectInfoXml(build, listener, module.getName());
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "ProjectInfo: " + project_info.getAbsolutePath());

			if (project_info.exists()) {
				ModuleAction m = new ModuleAction(build, module.getName());
				if (!m.build(project_info, listener)) {
					return false;
				}
				this.modules.add(m);
			}
		}

		return true;
	}

	public void doCoverageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CoveragePlot c = new CoveragePlot(this, "Coverage %");
		c.doCoverageGraph(req, rsp);
	}

	public void doCoverageMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CoveragePlot c = new CoveragePlot(this, "Coverage");
		c.doCoverageMap(req, rsp);
	}

	public void doTimeGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		TimePlot c = new TimePlot(this, "Time (minutes)");
		c.doTimeGraph(req, rsp);
	}

	public void doTimeMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
		TimePlot c = new TimePlot(this, "Time");
		c.doTimeMap(req, rsp);
	}
	
	// data for jelly template

	/**
	 * 
	 * @return
	 */
	public int getNumberOfModules() {
		return this.modules.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfTestableClasses() {
		if (this.modules.isEmpty()) {
			return 0;
		}

		int classes = 0;
		for (ModuleAction m : this.modules) {
			classes += m.getNumberOfTestableClasses();
		}

		return classes;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getCriteria() {
		Set<String> criteria = new LinkedHashSet<String>();
		if (this.modules.isEmpty()) {
			return criteria;
		}

		for (ModuleAction m : this.modules) {
			criteria.addAll(m.getCriteria());
		}

		return criteria;
	}
	/**
	 * 
	 * @return
	 */
	public double getOverallCoverage() {
		if (this.modules.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ModuleAction m : this.modules) {
			coverage += m.getOverallCoverage();
		}

		DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
		formatter.applyPattern("#0.00");
		return Double.parseDouble(formatter.format(coverage / this.modules.size()));
	}

	/**
	 * 
	 * @param criterionName
	 * @return
	 */
	public double getCriterionCoverage(String criterionName) {
		if (this.modules.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ModuleAction m : this.modules) {
			coverage += m.getCriterionCoverage(criterionName);
		}

		DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
		formatter.applyPattern("#0.00");
		return Double.parseDouble(formatter.format(coverage / this.modules.size()));
	}

	/**
	 * Return the total time (minutes) spent on test generation
	 * 
	 * @return 
	 */
	public int getTotalEffort() {
		if (this.modules.isEmpty()) {
			return 0;
		}

		int effort = 0;
		for (ModuleAction m : this.modules) {
			effort += m.getTotalEffort();
		}

		return effort;
	}
}
