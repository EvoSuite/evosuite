package org.evosuite.jenkins.plot;

import org.evosuite.jenkins.actions.BuildAction;
import org.evosuite.jenkins.actions.ProjectAction;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;

public class TimePlot extends Plot {

	public TimePlot(ProjectAction project, String yLabel) {
		super(project, yLabel);
	}

	public void doTimeGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CategoryDataset timeDataset = this.doStats();
		this.setCategoryDataset(timeDataset);

		if (ChartUtil.awtProblemCause != null) {
			rsp.sendRedirect2(req.getContextPath());
		} else {
			this.doPng(req, rsp);
		}
	}

	public void doTimeMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
		CategoryDataset timeDataset = this.doStats();
		this.setCategoryDataset(timeDataset);

		this.doMap(req, rsp);
	}

	private CategoryDataset doStats() {
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> timeDataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

		for (Run<?,?> build : this.project.getProject().getBuilds()) {
			final BuildAction build_action = build.getAction(BuildAction.class);
			if (build_action == null) {
				// no build action is associated with this build, so skip it
				continue;
			}

			int totalTime = build_action.getProjectAction().getTotalEffort();
			timeDataSetBuilder.add(totalTime, "TotalEffort", new ChartUtil.NumberOnlyBuildLabel(build));
		}

		return timeDataSetBuilder.build();
	}
}
