package org.evosuite.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterNodeRemote extends Remote {

	public static final String RMI_SERVICE_NAME = "MasterNode";
	
	public void registerClientNode(String clientRmiIdentifier) throws RemoteException;
	
	public void informChangeOfStateInClient(String clientRmiIdentifier, ClientState state) throws RemoteException;
}
