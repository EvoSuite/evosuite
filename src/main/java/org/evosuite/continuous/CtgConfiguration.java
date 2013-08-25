package org.evosuite.continuous;

import org.evosuite.Properties;
import org.evosuite.continuous.job.JobScheduler.AvailableSchedule;

/**
 * This class contain the starting, fixed configurations for CTG
 * @author arcuri
 *
 */
public class CtgConfiguration {

	public  final int totalMemoryInMB;	
	public final int numberOfCores;	
	public final int timeInMinutes;
	
	/**
	 * The minimum amount of minutes a search/job should run.
	 * Less than that, and there would be no point to even run
	 * the search.
	 */
	public final int minMinutesPerJob;
	public final boolean callHome;
	public final AvailableSchedule schedule;
	
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
