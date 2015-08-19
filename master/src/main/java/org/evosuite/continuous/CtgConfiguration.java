/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous;

import org.evosuite.Properties;
import org.evosuite.Properties.AvailableSchedule;

/**
 * This class contain the starting, fixed configurations for CTG
 * @author arcuri
 *
 */
public class CtgConfiguration {

	/**
	 * To run a job, you need a minimum of RAM.
	 * If not enough RAM, then no point in even trying to start
	 * a search.
	 * Note: this include the memory of both the master and
	 * clients together 
	 */
	protected final int MINIMUM_MEMORY_PER_JOB_MB = 500;
	
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

	/**
	 * Get new configuration with budget time proportional to the number of classes (and available cores)
	 * 
	 * 
	 * @param minutesPerClass
	 * @param numberOfCUTs
	 * @return
	 * @throws IllegalArgumentException
	 */
	public  CtgConfiguration getWithChangedTime(int minutesPerClass, int numberOfCUTs)
		throws IllegalArgumentException{
		
		if(minutesPerClass < 0){
			throw new IllegalArgumentException("Invalid value for minutesPerClass:" + minutesPerClass);
		}
		if(numberOfCUTs < 0){
			throw new IllegalArgumentException("Invalid value for numberOfCUTs:"+numberOfCUTs);
		}
		
		int time = (int) Math.ceil((minutesPerClass * numberOfCUTs) / (double) this.getNumberOfUsableCores());
		
		return new CtgConfiguration(
				this.totalMemoryInMB, 
				this.numberOfCores, 
				time, 
				this.minMinutesPerJob,
				this.callHome, 
				this.schedule
				);
	}
	
	
	
	/**
	 * We cannot use cores if we do not have enough memory,
	 * as each process has some minimum requirements
	 * 
	 * @return
	 */
	public int getNumberOfUsableCores() {
		if(numberOfCores * MINIMUM_MEMORY_PER_JOB_MB <=  totalMemoryInMB) {
			return numberOfCores;
		} else {
			return totalMemoryInMB / MINIMUM_MEMORY_PER_JOB_MB;
		}
	}

	public int getConstantMemoryPerJob(){
		return  totalMemoryInMB / getNumberOfUsableCores() ;
	}
}
