/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous.job;

import java.util.List;

import org.evosuite.Properties.AvailableSchedule;
import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.continuous.job.schedule.BudgetAndSeedingSchedule;
import org.evosuite.continuous.job.schedule.BudgetSchedule;
import org.evosuite.continuous.job.schedule.HistorySchedule;
import org.evosuite.continuous.job.schedule.ScheduleType;
import org.evosuite.continuous.job.schedule.SeedingSchedule;
import org.evosuite.continuous.job.schedule.SimpleSchedule;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
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
	
	
	private static Logger logger = LoggerFactory.getLogger(JobScheduler.class);


	private final ProjectStaticData projectData;
	
	protected  final CtgConfiguration configuration;

	private ScheduleType currentSchedule;
	
	/**
	 * Main constructor
	 * 
	 * @param projectData
	 */
	public JobScheduler(ProjectStaticData projectData,
			CtgConfiguration conf) {
		super();
		this.projectData = projectData;	
		this.configuration = conf;
		chooseScheduleType(configuration.schedule);
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
			case HISTORY:
                currentSchedule = new HistorySchedule(this);
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

		// update some extra information of each Class-Under-Test
		List<JobDefinition> jobs = currentSchedule.createNewSchedule();
		for (JobDefinition job : jobs) {
		  ClassInfo classInfo = this.projectData.getClassInfo(job.cut);
		  classInfo.setTimeBudgetInSeconds(job.seconds);
		  classInfo.setMemoryInMB(job.memoryInMB);
		}

		return jobs;
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
	
	public CtgConfiguration getConfiguration() {
		return configuration;
	}
}
