package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.util.List;

public class BuildAction implements Action {

	private final ProjectAction projectAction;
	private final AbstractBuild<?, ?> build;

	public BuildAction(ProjectAction projectAction, AbstractBuild<?, ?> build) {
		this.projectAction = projectAction;
		this.build = build;
	}

	public ProjectAction getProjectAction() {
		return this.projectAction;
	}

	public List<ModuleAction> getModules() {
		return this.projectAction.getModules();
	}

	public AbstractBuild<?, ?> getBuild() {
		return this.build;
	}

	public boolean build() {
		for (ModuleAction module_action : this.projectAction.getModules()) {
			module_action.build();
		}
		// FIXME should we return the result of build?
		return true;
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

	// data for jelly template

	public int getNumberOfModules() {
		return this.projectAction.getNumberOfModules();
	}

	public int getNumberOfTestableClasses() {
		return this.projectAction.getNumberOfTestableClasses();
	}

	public double getOverallCoverage() {
		return this.projectAction.getOverallCoverage();
	}
}
