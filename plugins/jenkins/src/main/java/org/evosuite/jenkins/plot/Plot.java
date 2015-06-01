package org.evosuite.jenkins.plot;

import hudson.model.AbstractProject;
import hudson.util.ColorPalette;
import hudson.util.Graph;

import java.util.Calendar;

import org.evosuite.jenkins.actions.ProjectAction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

public abstract class Plot extends Graph {

	protected ProjectAction project;
	private CategoryDataset dataset;
	private String yLabel;

	public Plot(ProjectAction project, String yLabel) {
		super(Calendar.getInstance(), 350, 150);

		this.project = project;
		this.yLabel = yLabel;
	}

	public void setCategoryDataset(CategoryDataset dataset) {
		this.dataset = dataset;
	}

	@Override
	protected JFreeChart createGraph() {
		final JFreeChart chart = ChartFactory.createLineChart(null, "Build Number #", this.yLabel, this.dataset, PlotOrientation.VERTICAL, true, true, true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		CategoryAxis domainAxis = new CategoryAxis();
		//domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);

		plot.setDomainAxis(domainAxis);

		ValueAxis yAxis = plot.getRangeAxis();
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		yAxis.setRange(0.0, 100.0);

		URLAndTooltipRenderer urlRenderer = new URLAndTooltipRenderer(this.project.getProject());
		ColorPalette.apply(urlRenderer);
		plot.setRenderer(urlRenderer);

		return chart;
	}

	private static class URLAndTooltipRenderer extends LineAndShapeRenderer {

		private static final long serialVersionUID = 1347738148294019295L;

		private AbstractProject<?, ?> project;

		public URLAndTooltipRenderer(AbstractProject<?, ?> prj) {
			this.project = prj;
		}

		@Override
		public CategoryToolTipGenerator getToolTipGenerator(int row, final int columnOuter) {
			return new URLAndTooltipBuilder(project);
		}

		@Override
		public CategoryURLGenerator getItemURLGenerator(int row, final int column) {
			return new URLAndTooltipBuilder(project);
		}

	}

	private static class URLAndTooltipBuilder implements CategoryURLGenerator, CategoryToolTipGenerator {

		private AbstractProject<?, ?> project;

		public URLAndTooltipBuilder(AbstractProject<?, ?> prj) {
			this.project = prj;
		}

		@Override
		public String generateURL(CategoryDataset dataset, int series, int category) {
			int da = Integer.parseInt((String) dataset.getColumnKey(category).toString().substring(1));
			return "/jenkins/" + project.getBuildByNumber(da).getUrl() + "evosuite-build/";
		}

		@Override
		public String generateToolTip(CategoryDataset dataset, int row, int column) {
			return "Build " + dataset.getColumnKey(column) + " - " + dataset.getValue(row, column);
		}
	}
}
