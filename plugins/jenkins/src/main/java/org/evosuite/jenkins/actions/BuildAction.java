package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.util.List;
import java.util.Set;

public class BuildAction implements Action {

	private final AbstractBuild<?, ?> build;

	private final ProjectAction projectAction;

	public BuildAction(AbstractBuild<?, ?> build, ProjectAction projectAction) {
		this.build = build;
		this.projectAction = projectAction;
	}

	@Override
	public String getIconFileName() {
		return "/plugin/evosuite-jenkins-plugin/icons/evosuite.png";
	}

	@Override
	public String getDisplayName() {
		return "EvoSuite Build Statistics";
	}

	@Override
	public String getUrlName() {
		return "evosuite-build";
	}

	public Object getDynamic(String token) {
		for (ModuleAction m : this.projectAction.getModules()) {
			if (m.getName().equals(token.replace("$", ":"))) {
				return m;
			}
		}

		return null;
	}
	/*public Object getDynamic(String token, org.kohsuke.stapler.StaplerRequest req, org.kohsuke.stapler.StaplerResponse rsp) {
		org.kohsuke.stapler.StaplerRequest _req = req;
		org.kohsuke.stapler.StaplerResponse _rsp = rsp;
		return null;
	}*/

	public AbstractBuild<?, ?> getBuild() {
		return this.build;
	}

	public ProjectAction getProjectAction() {
		return this.projectAction;
	}

	// data for jelly template
	
	public int getNumberOfModules() {
		return this.projectAction.getNumberOfModules();
	}

	public int getNumberOfTestableClasses() {
		return (int) this.projectAction.getNumberOfTestableClasses();
	}

	public Set<String> getCriteria() {
		return this.projectAction.getCriteria();
	}

	public double getOverallCoverage() {
		return this.projectAction.getOverallCoverage();
	}

	public double getCriterionCoverage(String criterionName) {
		return this.projectAction.getCriterionCoverage(criterionName);
	}

	public List<ModuleAction> getModules() {
		return this.projectAction.getModules();
	}
}
