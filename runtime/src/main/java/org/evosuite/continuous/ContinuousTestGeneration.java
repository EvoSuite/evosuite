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
 * 
 * <p>
 * TODO we should also have an option "regression" to run
 * current test suites, and see if any fails.
 * Even if do not want to explicitly do regression, we might still
 * have to run old test cases, as failing ones should be (re)moved and
 * labelled as "regression-failing"
 * 
 * 
 * <p>
 * TODO need also option to automatically commit to repository any new, better test
 * 
 * @author arcuri
 *
 */
public class ContinuousTestGeneration {


	/**
	 * Target folder/jar defining the SUT
	 */
	private final String target;
    
	/**
	 * Defines what classes in the target should be used by specifying 
	 * a common package prefix
	 */
	private final String prefix;
	
    /**
     *  The complete, used classpath
     */
    private final String projectClassPath;
	
    private CtgConfiguration configuration;
    
    public ContinuousTestGeneration(String target, String projectClassPath, String prefix, CtgConfiguration conf) {
		super();		
		this.target = target;
		this.prefix = prefix;
		this.projectClassPath = projectClassPath;
		this.configuration = conf;
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
    		storageOK = storage.createNewTmpFolders();
		if(!storageOK){
			return "Failed to create tmp folders";
		}  
			
    		//check project
    		ProjectAnalyzer analyzer = new ProjectAnalyzer(target,prefix);
    		ProjectStaticData data = analyzer.analyze();
    		
    		if(data.getTotalNumberOfTestableCUTs() == 0){
    			return "There is no class to test in the chosen project";
    		}
    		
    		if(Properties.CTG_TIME_PER_CLASS != null){
    			configuration = configuration.getWithChangedTime(Properties.CTG_TIME_PER_CLASS, data.getTotalNumberOfTestableCUTs());
    		}
    		
    		JobScheduler scheduler = new JobScheduler(data,configuration);
    		JobExecutor executor = new JobExecutor(storage,projectClassPath,configuration);
    		
    		//loop: define (partial) schedule
    		while(scheduler.canExecuteMore()){
    			List<JobDefinition> jobs = scheduler.createNewSchedule();
    			executor.executeJobs(jobs,configuration.getNumberOfUsableCores());
    			executor.waitForJobs();
    		}
    		
    		String description = storage.mergeAndCommitChanges(data);

    		//call home
    		if(configuration.callHome){
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
		ProjectInfo projectInfo = storage.getDatabaseProjectInfo(); 
		
		if(projectInfo==null){
			return "No info available";
		}
		
		//TODO all info
		
		StringBuilder sb = new StringBuilder();
		sb.append("Total number of classes in the project: "+
				projectInfo.getTotalNumberOfClasses()+"\n");
		sb.append("Number of classes in the project that are testable: "+
				projectInfo.getTotalNumberOfTestableClasses()+"\n");
    		sb.append("Number of generated test suites: "+
    				projectInfo.getGeneratedTestSuites().size()+"\n");
		sb.append("Average branch coverage: "+
    				projectInfo.getAverageBranchCoverage()+"\n");
    		
    		return sb.toString();
    }
}
