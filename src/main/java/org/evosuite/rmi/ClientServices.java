package org.evosuite.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.evosuite.Properties;
import org.evosuite.rmi.service.ClientNodeImpl;
import org.evosuite.rmi.service.ClientNodeLocal;
import org.evosuite.rmi.service.ClientNodeRemote;

public class ClientServices {

	private ClientNodeImpl clientNode;
	
	public boolean registerServices() throws RemoteException{
		int port = Properties.PROCESS_COMMUNICATION_PORT;
		Registry registry = LocateRegistry.getRegistry(port);
		clientNode = new ClientNodeImpl(registry);
		ClientNodeRemote stub = (ClientNodeRemote) UnicastRemoteObject.exportObject(clientNode,port);
		registry.rebind(clientNode.getClientRmiIdentifier(), stub);
		
		return clientNode.init();
	}

	public ClientNodeLocal getClientNode() {
		return clientNode;
	}
}
