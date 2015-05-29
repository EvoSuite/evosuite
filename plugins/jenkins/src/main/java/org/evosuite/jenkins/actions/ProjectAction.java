package org.evosuite.jenkins.actions;

import hudson.FilePath;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.Action;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.jenkins.plot.CoveragePlot;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ProjectAction implements Action {

	private AbstractProject<?, ?> project;
	private List<ModuleAction> modules = new ArrayList<ModuleAction>();

	public ProjectAction(AbstractProject<?, ?> project) {
		this.project = (AbstractProject<?, ?>) project;
	}

	public void perform(AbstractMavenProject<?, ?> project, FilePath workspace) {
		MavenModuleSet prj = (MavenModuleSet) this.project;
		for (MavenModule module : prj.getModules()) {
			Path path = Paths.get(workspace.getRemote()
					+ File.separator + (module.getRelativePath() != "" ? module.getRelativePath() + File.separator : "")
					+ ".evosuite" + File.separator + "project_info.xml");

			if (Files.exists(path)) {
				this.modules.add(new ModuleAction(module.getName(), path));
			}
		}
	}

	public AbstractProject<?, ?> getProject() {
		return this.project;
	}

	public List<ModuleAction> getModules() {
		return this.modules;
	}

	public void doCoverageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CoveragePlot c = new CoveragePlot(this, "Overall Coverage %", false);
		c.doCoverageGraph(req, rsp);
	}

	public void doCoverageMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CoveragePlot c = new CoveragePlot(this, "Overall Coverage", false);
		c.doCoverageMap(req, rsp);
	}

	public double getOverallCoverage() {
		if (this.modules.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ModuleAction module : this.modules) {
			coverage += module.getOverallCoverage();
		}

		return coverage / this.modules.size();
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

	// data for jelly template

	public int getNumberOfModules() {
		return this.modules.size();
	}

	public int getNumberOfTestableClasses() {
		if (this.modules.isEmpty()) {
			return 0;
		}

		int count = 0;
		for (ModuleAction module : this.modules) {
			count += module.getNumberOfTestableClasses();
		}

		return count;
	}
}
