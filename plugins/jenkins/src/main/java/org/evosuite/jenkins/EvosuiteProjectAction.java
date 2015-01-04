/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.evosuite.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Riki
 */
public class EvosuiteProjectAction implements Action{
    private final AbstractProject<?, ?> project;
    private CategoryDataset dataset;
    private CategoryDataset coverageDataset;
    private CategoryDataset classDataset;
    private CategoryDataset testableDataset;
    
    private final Boolean defaultgraph;
    private final Boolean dedicatedgraph;
    
    public EvosuiteProjectAction(AbstractProject<?, ?> project , EvosuiteRecorder recorder){
        this.project = project;
        this.defaultgraph = recorder.getDisplayDefaultProjectInfo();
        this.dedicatedgraph = recorder.getDisplayProjectInfo();
    }
    public boolean getDisplaydefault(){
        return defaultgraph;
    }
    public AbstractProject<?, ?> getProject() {
        return this.project;
    }

    public String getIconFileName() {
        if(dedicatedgraph){
            return "/plugin/evosuite-jenkins-plugin/icons/evosuite.png";
        }
        else{
            return null;
        }
    }

    public String getDisplayName() {
        return "Evosuite Project Graphs";
    }

    public String getUrlName() {
        return "evosuite-project";
    }
    
    
    public void doCoverageMap( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        doStats();
        getGraph(getCoverageDataset(), "Overall Coverage", false).doMap( req, rsp );
    }
    
    public void doCoverageGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        doStats();
        if( ChartUtil.awtProblemCause != null ){
            rsp.sendRedirect2( req.getContextPath());
            return;
        }
        getGraph(getCoverageDataset(), "Overall Coverage %", false).doPng( req, rsp);
    }
    public void doClassMap( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        doStats();
        getGraph(getClassDataset(), "Number Of Classes", true).doMap( req, rsp );
    }
    
    public void doClassGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        doStats();
        if( ChartUtil.awtProblemCause != null ){
            rsp.sendRedirect2( req.getContextPath());
            return;
        }
        getGraph(getClassDataset(), "Number Of Classes", true).doPng( req, rsp);
    }
    
    private void doStats(){
        final List<EvosuiteStats> adReportList = new ArrayList<EvosuiteStats>();
        final List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> coverageDataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> classDataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        for (AbstractBuild<?, ?> currentBuild : builds) {
            final EvosuiteBuildAction evosuiteBuildAction = currentBuild.getAction(EvosuiteBuildAction.class);
            if (evosuiteBuildAction == null){
                // no build action is associated with this build, so skip it
                continue;
            }
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(currentBuild);
            if (Double.isNaN(evosuiteBuildAction.OverallCoverage)){
                coverageDataSetBuilder.add(0, "overall coverage", label);
            }
            else{
                coverageDataSetBuilder.add(evosuiteBuildAction.OverallCoverage, "overall coverage", label);
            }
            classDataSetBuilder.add(evosuiteBuildAction.OverallNumberOfClasses, "classes", label);
            classDataSetBuilder.add(evosuiteBuildAction.OveralltotalNumberOfTestableClasses, "testable classes", label);
        }
        coverageDataset = coverageDataSetBuilder.build();
        classDataset = classDataSetBuilder.build();
    }
    
    public EvosuiteGraph getGraph(CategoryDataset data, String ylabel, Boolean legend){
        return new EvosuiteGraph(data, ylabel, legend, getProject());
    }
    public CategoryDataset getClassDataset(){
        return classDataset;
    }
    public CategoryDataset getCoverageDataset(){
        return coverageDataset;
    }
}
