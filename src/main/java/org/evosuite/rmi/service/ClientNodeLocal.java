package org.evosuite.rmi.service;

import org.evosuite.ga.Chromosome;

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
	
	public void trackOutputVariable(String name, Object value);

	public void waitUntilDone();  
}
