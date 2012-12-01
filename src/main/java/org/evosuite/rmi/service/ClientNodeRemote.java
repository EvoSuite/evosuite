package org.evosuite.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientNodeRemote extends Remote {

	
	public void startNewSearch() throws RemoteException;
	
	public void cancelCurrentSearch() throws RemoteException;
	
	public boolean waitUntilDone(long timeoutInMs) throws RemoteException, InterruptedException;
}
