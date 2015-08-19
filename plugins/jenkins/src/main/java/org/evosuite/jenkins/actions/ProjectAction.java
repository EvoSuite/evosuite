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

import hudson.FilePath;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.jenkins.plot.CoveragePlot;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
	
	public boolean perform(AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build) {
		FilePath workspace = build.getWorkspace();

		MavenModuleSet prj = (MavenModuleSet) this.project;
		for (MavenModule module : prj.getModules()) {
			Path project_info = Paths.get(workspace.getRemote()
					+ File.separator + (module.getRelativePath() != "" ? module.getRelativePath() + File.separator : "")
					+ ".evosuite" + File.separator + "project_info.xml");

			if (Files.exists(project_info)) {
				ModuleAction m = new ModuleAction(build, module.getName());
				if (!m.build(project_info)) {
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

		return coverage / this.modules.size();
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

		return coverage / this.modules.size();
	}
}
