package org.evosuite.jenkins;

import hudson.FilePath;
import hudson.maven.MavenModule;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EvosuiteBuildAction implements Action, Serializable {

	private static final long serialVersionUID = -4455682717560546675L;

	public Collection<MavenModule> validmodules;

	public Set <EvosuiteStats> es = new  HashSet<EvosuiteStats>();
	AbstractBuild<?, ?> build;
	public int num;
	public String averageBranchCoverage;
	public FilePath workspacedir;
	public int buildNumber;

	private Boolean displayBuildSummary;
	private Boolean displayDedicatedBuildInfo;

	public EvosuiteBuildAction(Collection<MavenModule>modules, AbstractBuild<?, ?> build, EvosuiteRecorder recorder) {
		this.displayBuildSummary = recorder.getDisplayBuildSummary();
		this.displayDedicatedBuildInfo = recorder.getDisplayBuildInfo();
		this.validmodules = modules;
		this.build = build;
		this.workspacedir = recorder.getWorkspaceDir();
		this.buildNumber = build.getNumber();
		this.displayBuildSummary = recorder.getDisplayBuildSummary();
		readStats();
	}
	public int OveralltotalNumberOfTestableClasses = 0;
	public int OverallNumberOfClasses = 0;
	public double OverallCoverage = 0;
	public List <EvosuiteStats> getEvosuiteStats(){
		return new ArrayList<EvosuiteStats> (es);
	}
	public AbstractBuild<?, ?> getBuild(){
		return this.build;
	}
	public boolean getDisplaysummary(){
		return displayBuildSummary;
	}
	public boolean getDisplayDedicatedBuildInfo(){
		return displayDedicatedBuildInfo;
	}
	public int getClasses(){
		return OverallNumberOfClasses;
	}
	public int getTestsableclasses(){
		return OveralltotalNumberOfTestableClasses;
	}
	public String getOverallCoverage(){
		DecimalFormat df = new DecimalFormat("#.00"); 
		if(Double.isNaN(OverallCoverage)){
			return "0";
		}
		else{
			return df.format(OverallCoverage);
		}

	}
	public final void readStats(){
		//for each valid module create an evosuite stats object that contains the stats from the xml file
		OveralltotalNumberOfTestableClasses = 0;
		OverallNumberOfClasses = 0;
		OverallCoverage = 0;
		Iterator<MavenModule> it = validmodules.iterator();
		int coverageNormaliser = 0;
        System.out.println("Iterating projects "+validmodules.size());

		while(it.hasNext()){
			MavenModule module = (MavenModule) it.next();
			File tempfile = new File(workspacedir.getRemote() + // File.separator + module.getModuleName().artifactId + 
					File.separator + ".continuous_evosuite" + File.separator + "project_info.xml");
			EvosuiteStats te = new EvosuiteStats (tempfile, buildNumber);
			es.add(te);
			if (Double.isNaN(te.getaverageBranchCoverage())){
				OverallCoverage = 0;
			}else{
				OverallCoverage += te.getaverageBranchCoverage();
			}
			OverallNumberOfClasses+= te.getNumberOfClasses();
			OveralltotalNumberOfTestableClasses += te.gettotalNumberOfTestableClasses();
			coverageNormaliser++;
		}
		OverallCoverage = 100*(OverallCoverage/coverageNormaliser);
	}


	/* (non-Javadoc)
	 * @see hudson.model.Action#getIconFileName()
	 */
	public String getIconFileName() {
		if(displayDedicatedBuildInfo){
			return "/plugin/evosuite-jenkins-plugin/icons/evosuite.png";
		}
		else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see hudson.model.Action#getDisplayName()
	 */
	public String getDisplayName() {
		return "Evosuite Build Stats";
	}

	/* (non-Javadoc)
	 * @see hudson.model.Action#getUrlName()
	 */
	public String getUrlName() {
		return "evosuite-build";
	}
}
