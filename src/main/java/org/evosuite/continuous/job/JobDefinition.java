package org.evosuite.continuous.job;

public class JobDefinition {

	public final int seconds;
	public final int memoryInMB; 
	public final String cut;
	public final int configurationId;
	
	public JobDefinition(int seconds, int memoryInMB, String cut,
			int configurationId) {
		super();
		this.seconds = seconds;
		this.memoryInMB = memoryInMB;
		this.cut = cut;
		this.configurationId = configurationId;
	}
}
