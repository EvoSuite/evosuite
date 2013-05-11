package org.evosuite.continuous.job;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.continuous.job.schedule.ScheduleType;
import org.evosuite.continuous.job.schedule.SimpleSchedule;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.continuous.project.ProjectStaticData;

/**
 * Class used to define which classes should be used as CUT for this CTG execution,
 * and how to allocate the search budget
 * 
 * @author arcuri
 *
 */
public class JobScheduler {
	
	private final ProjectStaticData projectData;
	private final StorageManager storageManager;
	private final int numberOfCores;
	private final int totalBudgetInMinutes;
	
	private ScheduleType currentSchedule; 
	
	/**
	 * List of all available types of schedule this scheduler can choose to use
	 */
	public static final List<String> SCHEDULE_TYPES = Collections.unmodifiableList(Arrays.asList(new
			String[]{
			SimpleSchedule.NAME
	}));
	
	public JobScheduler(ProjectStaticData projectData,
			StorageManager storageManager, int numberOfCores,
			int totalBudgetInMinutes) {
		super();
		this.projectData = projectData;
		this.storageManager = storageManager;
		this.numberOfCores = numberOfCores;
		this.totalBudgetInMinutes = totalBudgetInMinutes;
		
		/*
		 * TODO: default one should be the best found in the experiments
		 */
		currentSchedule = new SimpleSchedule(this);
	}
	
	public void chooseScheduleType(String scheduleName) throws IllegalArgumentException{
		//TODO
	}

	public List<JobDefinition> createNewSchedule(){
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

	public StorageManager getStorageManager() {
		return storageManager;
	}

	public int getNumberOfCores() {
		return numberOfCores;
	}

	public int getTotalBudgetInMinutes() {
		return totalBudgetInMinutes;
	}
	
	
}
