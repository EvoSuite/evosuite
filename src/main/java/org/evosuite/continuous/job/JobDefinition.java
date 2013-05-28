package org.evosuite.continuous.job;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Definition of a "job", ie a run of EvoSuite on a CUT.
 * 
 * <p>
 * Note: this class is/should be immutable
 * 
 * @author arcuri
 *
 */
public class JobDefinition {

	private static final AtomicInteger counter = new AtomicInteger(0);
	
	/**
	 * A unique, human-readable identifier for this job
	 */
	public final int jobID;	
	
	public final int seconds;
	public final int memoryInMB; 
	public final String cut;
	public final int configurationId;
	
	/**
	 * the ids of all the other jobs this one depends on,
	 * and that to be execute before starting this job
	 */
	public final Set<Integer> dependentOnIDs;
	
	public JobDefinition(int seconds, int memoryInMB, String cut,
			int configurationId, Set<Integer> dependencies) {
		super();
		this.jobID = counter.getAndIncrement();
		this.seconds = seconds;
		this.memoryInMB = memoryInMB;
		this.cut = cut;
		this.configurationId = configurationId;
		if(dependencies!=null){
			this.dependentOnIDs = Collections.unmodifiableSet(new HashSet<Integer>(dependencies));
		} else {
			this.dependentOnIDs = null;
		}
	}
}
