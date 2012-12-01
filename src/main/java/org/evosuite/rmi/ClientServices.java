package org.evosuite.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.evosuite.Properties;
import org.evosuite.rmi.service.ClientNodeImpl;
import org.evosuite.rmi.service.ClientNodeRemote;

public class ClientServices {

	private ClientNodeImpl clientNode;
	
	public void registerServices() throws RemoteException{
		int port = Properties.PROCESS_COMMUNICATION_PORT;
		Registry registry = LocateRegistry.getRegistry(port);
		clientNode = new ClientNodeImpl(registry);
		ClientNodeRemote stub = (ClientNodeRemote) UnicastRemoteObject.exportObject(clientNode);
		registry.rebind(clientNode.getClientRmiIdentifier(), stub);
	}

	public ClientNodeImpl getClientNode() {
		return clientNode;
	}
}
