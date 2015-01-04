/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.evosuite.jenkins;

import hudson.model.AbstractProject;
import hudson.util.ColorPalette;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import java.util.Calendar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 *
 * @author Riki
 */
public class EvosuiteGraph extends Graph{
    private final CategoryDataset dataset;
    private final String yLabel;
    private final boolean legend;
    private final AbstractProject<?, ?> project;
    public EvosuiteGraph(CategoryDataset data, String ylabel, boolean legend, AbstractProject<?, ?> prj){
        super( Calendar.getInstance(), 350, 150);
        this.dataset = data;
        this.yLabel = ylabel;
        this.legend = legend;
        this.project = prj;
    }
    public  AbstractProject<?, ?> getProject(){
        return project;
    }

    @Override
    protected JFreeChart createGraph() {
        final JFreeChart chart = ChartFactory.createLineChart(
        null,
        "Build Number #",
        yLabel,
        dataset,
        PlotOrientation.VERTICAL,
        legend,
        true,
        true
        );
        CategoryPlot plot =  (CategoryPlot) chart.getPlot();
        ValueAxis yAxis = plot.getRangeAxis();
        CategoryAxis domainAxis = new ShiftedCategoryAxis( "Build Number" );
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );
//        domainAxis.
        if(legend){
            yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        }else{
            yAxis.setRange(0, 100);
        }
        URLAndTooltipRenderer urlRenderer = new URLAndTooltipRenderer(getProject());
        ColorPalette.apply( urlRenderer );
        plot.setRenderer( urlRenderer );

        return chart;
    }
	
	private static class URLAndTooltipRenderer extends LineAndShapeRenderer{

		private static final long serialVersionUID = 8548750757600199224L;
		
		AbstractProject<?, ?> project;

		public URLAndTooltipRenderer( AbstractProject<?, ?> prj){
                    this.project = prj;
		}
		
		@Override
    	public CategoryToolTipGenerator getToolTipGenerator( int row, final int columnOuter ){
    		return new URLAndTooltipBuilder(project);
    	}
		
    	@Override
    	public CategoryURLGenerator getItemURLGenerator( int row, final int column ){
    		return new URLAndTooltipBuilder(project);
    	}
		
	}
	
	private static class URLAndTooltipBuilder implements CategoryURLGenerator, CategoryToolTipGenerator{
        
        //    private int buildNumber;
        private AbstractProject<?, ?> project;
        public URLAndTooltipBuilder(AbstractProject<?, ?> prj ){
            this.project = prj;
        }
        
		public String generateURL( CategoryDataset dataset, int series, int category ){
                    int da = Integer.parseInt((String)dataset.getColumnKey(category).toString().substring(1));
			return "/jenkins/" + project.getBuildByNumber(da).getUrl();
		}

		public String generateToolTip( CategoryDataset dataset, int row, int column ){
			return "Build " + dataset.getColumnKey(column) + " - " + dataset.getValue(row, column);
		}
		
	}
    
}
