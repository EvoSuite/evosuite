package org.evosuite.continuous;

import java.util.List;

import org.evosuite.Properties;
import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobExecutor;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.job.JobScheduler.AvailableSchedule;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.continuous.project.ProjectAnalyzer;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.xsd.ProjectInfo;


/**
 * <p>
 * This is the main entry point for the new 
 * paradigm "Continuous Test Generation" (CTG).
 * This can be called directly from EvoSuite for
 * experiment reasons, but its usage for practitioners
 * will be based on build environments like Maven/Ant
 * and continuous integration environments like 
 * Jenkins/Hudson/Bamboo 
 * </p>
 * 
 * <p>
 * In a nutshell, the idea is for a project to keep track/store
 * the best test suite for each class in the project.
 * Each time CTG is run (and can be run continuously), we try
 * to generate test suites for classes we haven't addressed
 * yet, and we try to improve the current ones (by seeding).
 * Benefits are for example:
 * <ul>  
 * <li> No need to download EvoSuite. This is done automatically
 * with Maven </li>
 * <li> Smart allocation of search budget among classes with
 * different size/difficulty</li>
 * <li><b>Very</b> easily configurable to run 24/7, with reports on entire
 * project</li>
 * <li> Possibility to reuse test cases/data from one class to
 * help generation of test suites for new classes</li>
 * <li>Can automatically check from repository which classes
 * have been modified, and concentrate search on them</li>
 * </ul>
 * </p>
 * 
 * <p>
 * CTG can also be configured to "call home".
 * This will be useful to see how EvoSuite is used in practice.
 * </p>
 * 
 * @author arcuri
 *
 */
public class ContinuousTestGeneration {

	private final int totalMemoryInMB;	
    private final int numberOfCores;	
    private final int timeInMinutes;
	private final boolean callHome;
    private final String target;
    private final String projectClassPath;
	private final AvailableSchedule schedule;
    
    public ContinuousTestGeneration(String target, String projectClassPath, int memoryInMB, int numberOfCores,
			int timeInMinutes, boolean callHome, AvailableSchedule schedule) {
		super();		
		this.target = target;
		this.projectClassPath = projectClassPath;
		this.totalMemoryInMB = memoryInMB;
		this.numberOfCores = numberOfCores;
		this.timeInMinutes = timeInMinutes;
		this.callHome = callHome;
		this.schedule = schedule;
	}
	
    /**
     * Apply CTG, and return a string with some summary
     * 
     * @return
     */
    public String execute(){

    		//init the local storage manager
    		StorageManager storage = new StorageManager();
    		boolean storageOK = storage.openForWriting();
    		if(!storageOK){
    			return "Failed to initialize local storage system";
    		}
    		
    		//check project
    		ProjectAnalyzer analyzer = new ProjectAnalyzer(target);
    		ProjectStaticData data = analyzer.analyze();
    		
    		JobScheduler scheduler = new JobScheduler(data,storage,numberOfCores,totalMemoryInMB,timeInMinutes);
    		scheduler.chooseScheduleType(schedule);
    		JobExecutor executor = new JobExecutor(storage,timeInMinutes,projectClassPath,totalMemoryInMB);
    		
    		//loop: define (partial) schedule
    		while(scheduler.canExecuteMore()){
    			List<JobDefinition> jobs = scheduler.createNewSchedule();
    			executor.executeJobs(jobs);
    			executor.waitForJobs();
    		}
    		
    		storage.removeNoMoreExistentData(data);
    		String description = storage.mergeAndCommitChanges();

    		//call home
    		if(callHome){
    			//TODO
    		}
    		
    		return description;
    }
    
    /**
     * Clean all persistent data (eg files on disk) that
     * CTG has created so far
     */
    public boolean clean(){
    		StorageManager storage = new StorageManager();
    		return storage.clean();
    }
    
    /**
     * Get info on the current test cases in the database
     * @return
     */
    public String info(){
    		
		StorageManager storage = new StorageManager();    	
		ProjectInfo projectInfo = storage.getProjectInfo(); 
		
		if(projectInfo==null){
			return "No info available";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Number of classes in the project: "+
				projectInfo.getNumberOfClasses()+"\n");
    		sb.append("Number of generated test suites: "+
    				projectInfo.getGeneratedTests().getTests().size()+"\n");
		sb.append("Average branch coverage: "+
    				projectInfo.getAverageBranchCoverage()+"\n");
    		
    		return sb.toString();
    }
}
