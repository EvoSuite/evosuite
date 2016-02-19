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
