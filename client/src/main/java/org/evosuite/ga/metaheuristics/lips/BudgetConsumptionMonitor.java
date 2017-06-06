package org.evosuite.ga.metaheuristics.lips;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used to keep track of the execution time needed to reach the maximum coverage
 * 
 * @author AnonymousTester
 */
public class BudgetConsumptionMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(BudgetConsumptionMonitor.class);
	
	/** Coverage achieved in the previous generation */
	private double past_coverage;
	
	/** To keep track when the overall search started */
	private long startGlobalSearch;
	
	/** Time required to achieve the maximum coverage */
	private long time2MaxCoverage;
	
	/** 
	 * Constructor that initialises the counters 
	 */
	public BudgetConsumptionMonitor(){
		past_coverage = 0;
		startGlobalSearch =  System.currentTimeMillis();
		time2MaxCoverage = 0;
	}
	
	/** 
	 * This method updates the time needed to reach the maximum coverage if 
	 * the new coverage is greater than the previous one stored in "past_coverage"
	 * @param coverage new coverage value
	 */
	public void checkMaxCoverage(double coverage){
		if (coverage > past_coverage){
			past_coverage = coverage;
			time2MaxCoverage = System.currentTimeMillis() - startGlobalSearch;
			logger.debug("Time to reach max coverage updated to {}", time2MaxCoverage);
		}
	}
	
	public long getTime2MaxCoverage(){
		return this.time2MaxCoverage;
	}

}
