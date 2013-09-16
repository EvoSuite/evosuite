package org.evosuite.continuous.job;

import java.util.List;

import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.continuous.job.schedule.BudgetAndSeedingSchedule;
import org.evosuite.continuous.job.schedule.BudgetSchedule;
import org.evosuite.continuous.job.schedule.ScheduleType;
import org.evosuite.continuous.job.schedule.SeedingSchedule;
import org.evosuite.continuous.job.schedule.SimpleSchedule;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.continuous.project.ProjectStaticData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to define which classes should be used as CUT for this CTG execution,
 * and how to allocate the search budget
 * 
 * @author arcuri
 *
 */
public class JobScheduler {
	
	/**
	 * The types of schedules that can be used
	 * @author arcuri
	 *
	 */
	public enum AvailableSchedule {SIMPLE,BUDGET,SEEDING,BUDGET_AND_SEEDING}; 
	
	private static Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	/**
	 * To run a job, you need a minimum of RAM.
	 * If not enough RAM, then no point in even trying to start
	 * a search.
	 * Note: this include the memory of both the master and
	 * clients together 
	 */
	protected final int MINIMUM_MEMORY_PER_JOB_MB = 500;
	
	private final ProjectStaticData projectData;
	
	private final CtgConfiguration configuration;

	private ScheduleType currentSchedule;
	
	/**
	 * Main constructor
	 * 
	 * @param projectData
	 * @param numberOfCores
	 * @param totalMemoryInMB
	 * @param totalBudgetInMinutes
	 * @param minMinutesPerJob
	 */
	public JobScheduler(ProjectStaticData projectData,
			CtgConfiguration conf) {
		super();
		this.projectData = projectData;	
		this.configuration = conf;
		chooseScheduleType(configuration.schedule);
	}
	
	/**
	 * We cannot use cores if we do not have enough memory,
	 * as each process has some minimum requirements
	 * 
	 * @return
	 */
	public int getNumberOfUsableCores() {
		if(configuration.numberOfCores * MINIMUM_MEMORY_PER_JOB_MB <=  getTotalMemoryInMB()) {
			return configuration.numberOfCores;
		} else {
			return getTotalMemoryInMB() / MINIMUM_MEMORY_PER_JOB_MB;
		}
	}

	public int getConstantMemoryPerJob(){
		return  getTotalMemoryInMB() / getNumberOfUsableCores() ;
	}
	
	public int getMinSecondsPerJob(){
		return configuration.minMinutesPerJob * 60;
	}
	
	public void chooseScheduleType(AvailableSchedule schedule) throws IllegalArgumentException{

		switch(schedule){
			case SIMPLE:
				currentSchedule = new SimpleSchedule(this);
				break;
			case BUDGET:
				currentSchedule = new BudgetSchedule(this);
				break;
			case SEEDING:
				currentSchedule = new SeedingSchedule(this);
				break;
			case BUDGET_AND_SEEDING:
				currentSchedule = new BudgetAndSeedingSchedule(this);
				break;
			default:
				throw new IllegalArgumentException("Schedule '"+schedule+"' is not supported");				
		}
	}

	/**
	 * Return new schedule, or <code>null</code> if scheduling is finished
	 * @return
	 */
	public List<JobDefinition> createNewSchedule(){
		if(!canExecuteMore()){
			logger.info("Cannot schedule more jobs");
			return null;
		}
		logger.info("Creating new schedule with "+currentSchedule.getClass().getSimpleName());
		return currentSchedule.createNewSchedule();
	}
	

	
	/**
	 * When we get a schedule, the scheduler might decide to do not use the entire
	 * budget. Reason? It might decide to generate some test cases first, and then 
	 * use those as seeding for a new round of execution
	 * 
	 * @return
	 */
	public boolean canExecuteMore(){
		return currentSchedule.canExecuteMore();
	}

	public ProjectStaticData getProjectData() {
		return projectData;
	}
	
	public int getTotalBudgetInMinutes() {
		return configuration.timeInMinutes;
	}

	public int getTotalMemoryInMB() {
		return configuration.totalMemoryInMB;
	}	
}
