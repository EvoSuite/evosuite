package org.evosuite.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.evosuite.ga.Chromosome;

/**
 * Master Node view in the client process.  
 * @author arcuri
 *
 */
public interface MasterNodeRemote extends Remote {

	public static final String RMI_SERVICE_NAME = "MasterNode";
	
	public void registerClientNode(String clientRmiIdentifier) throws RemoteException;
	
	public void informChangeOfStateInClient(String clientRmiIdentifier, ClientState state, ClientStateInformation information) throws RemoteException;
	
	public void collectStatistics(String clientRmiIdentifier, Chromosome individual) throws RemoteException;

	public void collectStatistics(String clientRmiIdentifier, String name, Object value) throws RemoteException;
}
