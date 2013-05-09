package org.evosuite.continuous;

import org.evosuite.continuous.persistency.StorageManager;


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

	private final int memoryInMB;	
    private final int numberOfCores;	
    private final int timeInMinutes;
	private final boolean callHome;
    
    public ContinuousTestGeneration(int memoryInMB, int numberOfCores,
			int timeInMinutes, boolean callHome) {
		super();
		this.memoryInMB = memoryInMB;
		this.numberOfCores = numberOfCores;
		this.timeInMinutes = timeInMinutes;
		this.callHome = callHome;
	}
	
    /**
     * Apply CTG, and return a string with some summary
     * 
     * @return
     */
    public String execute(){

    		StorageManager storage = new StorageManager();
    		boolean storageOK = storage.open();
    		if(!storageOK){
    			return "Failed to initialize local storage system";
    		}
    		
    		//check project
    	
    		//check SVN/Git
    	
    		//loop: define (partial) schedule
    	
    		//call home
    	
    		//TODO
    		return null;
    }
    
    /**
     * Clean all persistent data (eg files on disk) that
     * CTG has created so far
     */
    public void clean(){
    		//TODO
    }
    
    /**
     * Get info on the current test cases in the database
     * @return
     */
    public String info(){
    		//TODO
    		return null;
    }
}
