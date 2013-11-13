package org.evosuite.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.evosuite.ga.Chromosome;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.statistics.RuntimeVariable;

/**
 * Master Node view in the client process.  
 * @author arcuri
 *
 */
public interface MasterNodeRemote extends Remote {

	public static final String RMI_SERVICE_NAME = "MasterNode";
	
	/*
	 * Note: we need names starting with 'evosuite' here, because those names are accessed 
	 * through reflections and used in the checks of the sandbox 
	 */
	
	public void evosuite_registerClientNode(String clientRmiIdentifier) throws RemoteException;
	
	public void evosuite_informChangeOfStateInClient(String clientRmiIdentifier, ClientState state, ClientStateInformation information) throws RemoteException;
	
	public void evosuite_collectStatistics(String clientRmiIdentifier, Chromosome individual) throws RemoteException;

	public void evosuite_collectStatistics(String clientRmiIdentifier, RuntimeVariable variable, Object value) throws RemoteException;

	public void evosuite_collectTestGenerationResult(String clientRmiIdentifier, TestGenerationResult result) throws RemoteException;
}
