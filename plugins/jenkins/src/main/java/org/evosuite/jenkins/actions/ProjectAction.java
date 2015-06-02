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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.jenkins.plot.CoveragePlot;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ProjectAction implements Action {

	private AbstractProject<?, ?> project;
	private Map<String, ModuleAction> modules = new LinkedHashMap<String, ModuleAction>();

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
				this.modules.put(module.getName(), new ModuleAction(module.getName(), path));
			}
		}
	}

	public AbstractProject<?, ?> getProject() {
		return this.project;
	}

	public Map<String, ModuleAction> getModules() {
		return this.modules;
	}

	public void doCoverageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CoveragePlot c = new CoveragePlot(this, "Coverage %");
		c.doCoverageGraph(req, rsp);
	}

	public void doCoverageMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CoveragePlot c = new CoveragePlot(this, "Coverage");
		c.doCoverageMap(req, rsp);
	}

	public double getOverallCoverage() {
		if (this.modules.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ModuleAction module : this.modules.values()) {
			coverage += module.getOverallCoverage();
		}

		return coverage / this.modules.size();
	}

	public Map<String, List<Double>> getCoverageValues() {
		Map<String, List<Double>> coverageValues = new LinkedHashMap<String, List<Double>>();

		for (ModuleAction module : this.modules.values()) {
			for (String criterion : module.getCoverageValues().keySet()) {
				List<Double> coverages = new ArrayList<Double>();
				if (coverageValues.containsKey(criterion)) {
					coverages = coverageValues.get(criterion);
				}

				coverages.addAll( module.getCoverageValues().get(criterion) );
				coverageValues.put(criterion, coverages);
			}
		}

		return coverageValues;
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
		for (ModuleAction module : this.modules.values()) {
			count += module.getNumberOfTestableClasses();
		}

		return count;
	}

	public Map<String, String> getCriterion() {
		if (this.modules.isEmpty()) {
			return null;
		}

		Set<String> names = new LinkedHashSet<String>();
		for (ModuleAction module : this.modules.values()) {
			names.addAll(module.getCriterion());
		}

		Map<String, String> coverageValues = new LinkedHashMap<String, String>();
		for (String criterionName : names) {
			NumberFormat formatter = new DecimalFormat("#0.00");
			coverageValues.put(criterionName, formatter.format(this.getCriterionCoverage(criterionName)));
		}

		return coverageValues;
	}

	private double getCriterionCoverage(String criterion) {
		if (this.modules.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ModuleAction module : this.modules.values()) {
			coverage += module.getCriterionCoverage(criterion);
		}

		return coverage / this.modules.size();
	}
}
