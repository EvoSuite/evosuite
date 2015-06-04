package org.evosuite.jenkins.plot;

import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

import java.io.IOException;
import java.util.Set;

import org.evosuite.jenkins.actions.BuildAction;
import org.evosuite.jenkins.actions.ProjectAction;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CoveragePlot extends Plot {

	public CoveragePlot(ProjectAction project, String yLabel) {
		super(project, yLabel);
	}

	public void doCoverageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CategoryDataset coverageDataset = this.doStats();
		this.setCategoryDataset(coverageDataset);

		if (ChartUtil.awtProblemCause != null) {
			rsp.sendRedirect2(req.getContextPath());
		} else {
			this.doPng(req, rsp);
		}
	}

	public void doCoverageMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CategoryDataset coverageDataset = this.doStats();
		this.setCategoryDataset(coverageDataset);

		this.doMap(req, rsp);
	}

	private CategoryDataset doStats() {
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> coverageDataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

		for (Run<?,?> build : this.project.getProject().getBuilds()) {
			final BuildAction build_action = build.getAction(BuildAction.class);
			if (build_action == null) {
				// no build action is associated with this build, so skip it
				continue;
			}

			Set<String> criteria = build_action.getProjectAction().getCriteria();
			for (String criterion : criteria) {
				double coverage = build_action.getProjectAction().getCriterionCoverage(criterion);
				coverageDataSetBuilder.add(coverage, criterion, new ChartUtil.NumberOnlyBuildLabel(build));
			}
		}

		return coverageDataSetBuilder.build();
	}
}
