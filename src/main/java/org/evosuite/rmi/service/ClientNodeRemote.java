package org.evosuite.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Client Node view in the master process.
 * 
 * @author arcuri
 * 
 */

public interface ClientNodeRemote extends Remote {

	public void startNewSearch() throws RemoteException;

	public void cancelCurrentSearch() throws RemoteException;

	public boolean waitUntilDone(long timeoutInMs) throws RemoteException,
	        InterruptedException;

	public void doCoverageAnalysis() throws RemoteException;

	public void printClassStatistics() throws RemoteException;
}
