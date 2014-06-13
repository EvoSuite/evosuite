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

	/**
	 * 
	 * @param timeoutInMs  maximum amount of time we can wait for the client to finish
	 * @return <code>true</code> if client is finished
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	public boolean waitUntilFinished(long timeoutInMs) throws RemoteException,
	        InterruptedException;

	public void doCoverageAnalysis() throws RemoteException;
	
	public void doDependencyAnalysis(String fileName) throws RemoteException;

	public void printClassStatistics() throws RemoteException;
}
