package org.evosuite.rmi.service;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.RuntimeVariable;

/**
 * Client Node view in the client process.  
 * @author arcuri
 *
 */
public interface ClientNodeLocal {

	public boolean init();
	
	public void changeState(ClientState state);

	public void changeState(ClientState state, ClientStateInformation information);
	
	public void updateStatistics(Chromosome individual);
	
	public void trackOutputVariable(RuntimeVariable variable, Object value);

	public void waitUntilDone();  
}
