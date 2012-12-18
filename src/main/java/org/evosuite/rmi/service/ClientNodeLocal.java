package org.evosuite.rmi.service;

/**
 * Client Node view in the client process.  
 * @author arcuri
 *
 */
public interface ClientNodeLocal {

	public boolean init();
	
	public void changeState(ClientState state);

	public void changeState(ClientState state, ClientStateInformation information);

	public void waitUntilDone();  
}
