package org.evosuite.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.evosuite.Properties.NoSuchParameterException;
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

	public void evosuite_collectTestGenerationResult(String clientRmiIdentifier, List<TestGenerationResult> results) throws RemoteException;

	public void evosuite_flushStatisticsForClassChange(String clientRmiIdentifier) throws RemoteException;

	public void evosuite_updateProperty(String clientRmiIdentifier, String propertyName, Object value) throws RemoteException, IllegalArgumentException, IllegalAccessException, NoSuchParameterException;
}
