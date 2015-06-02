package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Map;

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

	public Collection<ModuleAction> getModules() {
		return this.projectAction.getModules().values();
	}

	public AbstractBuild<?, ?> getBuild() {
		return this.build;
	}

	public boolean build() {
		for (ModuleAction module_action : this.projectAction.getModules().values()) {
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

	public String getOverallCoverage() {
		NumberFormat formatter = new DecimalFormat("#0.00");
		return formatter.format(this.projectAction.getOverallCoverage());
	}

	public Map<String, String> getCriterion() {
		return this.projectAction.getCriterion();
	}
}
