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
package org.evosuite.jenkins.actions;

import org.evosuite.Properties;
import org.evosuite.jenkins.plot.CoveragePlot;
import org.evosuite.jenkins.plot.TimePlot;
import org.evosuite.jenkins.recorder.EvoSuiteRecorder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import hudson.remoting.VirtualChannel;

public class ProjectAction implements Action {

	private final AbstractProject<?, ?> project;

	private final List<ModuleAction> modules;

	public ProjectAction(AbstractProject<?, ?> project) {
		this.project = (AbstractProject<?, ?>) project;
		this.modules = new ArrayList<ModuleAction>();
	}

	public ProjectAction(AbstractProject<?, ?> project, List<ModuleAction> modules) {
		this.project = (AbstractProject<?, ?>) project;
		this.modules = new ArrayList<ModuleAction>(modules);
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

	public void perform(AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build,
			BuildListener listener) throws InterruptedException, IOException {

		EnvVars env = build.getEnvironment(listener);
		env.overrideAll(build.getBuildVariables());

		VirtualChannel channel = build.getWorkspace().getChannel();

		MavenModuleSet prj = (MavenModuleSet) this.project;
		for (MavenModule module : prj.getModules()) {

		  FilePath fp = new FilePath(channel, build.getWorkspace().getRemote() + File.separator
              + (module.getRelativePath().isEmpty() ? "" : module.getRelativePath() + File.separator)
              + Properties.CTG_DIR + File.separator + Properties.CTG_PROJECT_INFO);

		  if (!fp.exists()) {
		    listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "There is not any " +
		        fp.getRemote() + " file for module " + module.getName());
		    continue ;
		  }

		  ByteArrayOutputStream out = new ByteArrayOutputStream();
		  fp.copyTo(out);
		  ByteArrayInputStream projectXML = new ByteArrayInputStream(out.toByteArray());

		  listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Analysing " +
		      Properties.CTG_PROJECT_INFO + " file from " + fp.getRemote());

		  ModuleAction m = new ModuleAction(build, module.getName());
		  if (!m.build(channel, projectXML, listener)) {
		    continue ;
		  }

		  this.modules.add(m);
		}
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

	public int getNumberOfModules() {
		return this.modules.size();
	}

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

	public int getNumberOfTestedClasses() {
		if (this.modules.isEmpty()) {
			return 0;
		}

		int classes = 0;
		for (ModuleAction m : this.modules) {
			classes += m.getNumberOfTestedClasses();
		}

		return classes;
	}

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

	public double getCriterionCoverage(String criterionName) {
		if (this.modules.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ModuleAction m : this.modules) {
			coverage += m.getAverageCriterionCoverage(criterionName);
		}

		DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
		formatter.applyPattern("#0.00");
		return Double.parseDouble(formatter.format(coverage / this.modules.size()));
	}

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

	public int getTimeBudget() {
      if (this.modules.isEmpty()) {
          return 0;
      }

      int effort = 0;
      for (ModuleAction m : this.modules) {
          effort += m.getTimeBudget();
      }

      return effort;
  }
}
