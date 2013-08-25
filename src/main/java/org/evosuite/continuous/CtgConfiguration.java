package org.evosuite.continuous;

import org.evosuite.Properties;
import org.evosuite.continuous.job.JobScheduler.AvailableSchedule;

/**
 * This class contain the starting, fixed configurations for CTG
 * @author arcuri
 *
 */
public class CtgConfiguration {

	/**
	 * how much max memory should be used at the same time
	 * among all the parallel CTG runs? 
	 */
	public  final int totalMemoryInMB;	
	
	/**
	 * Number of cores CTG is allowed to use
	 */
	public final int numberOfCores;	
	
	/**
	 * for how long CTG is allowed to run
	 */
	public final int timeInMinutes;
	
	/**
	 * The minimum amount of minutes a search/job should run.
	 * Less than that, and there would be no point to even run
	 * the search.
	 */
	public final int minMinutesPerJob;
	
	/**
	 * Should we call home to upload status/usage statistics?
	 */
	public final boolean callHome;
	
	/**
	 * The type of job scheduler CTG will use
	 */
	public final AvailableSchedule schedule;
	
	/**
	 * Main constructor
	 * 
	 * @param totalMemoryInMB
	 * @param numberOfCores
	 * @param timeInMinutes
	 * @param minMinutesPerJob
	 * @param callHome
	 * @param schedule
	 */
	public CtgConfiguration(int totalMemoryInMB, int numberOfCores,
			int timeInMinutes, int minMinutesPerJob, boolean callHome,
			AvailableSchedule schedule) {
		super();
		this.totalMemoryInMB = totalMemoryInMB;
		this.numberOfCores = numberOfCores;
		this.timeInMinutes = timeInMinutes;
		this.minMinutesPerJob = minMinutesPerJob;
		this.callHome = callHome;
		this.schedule = schedule;
	}
    
	/**
	 * Get instance based on values in {@link Properties}
	 * @return
	 */
	public static CtgConfiguration getFromParameters(){
		return new CtgConfiguration(
				Properties.CTG_MEMORY, 
				Properties.CTG_CORES, 
				Properties.CTG_TIME, 
				Properties.CTG_MIN_TIME_PER_JOB,
				false, /* TODO: just for now, as not implemented yet */
				Properties.CTG_SCHEDULE
				);
	}
}
