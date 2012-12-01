package org.evosuite.rmi.service;

public interface ClientNodeLocal {

	public boolean init();
	
	public void changeState(ClientState state);
}
