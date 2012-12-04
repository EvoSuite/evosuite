package org.evosuite.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.evosuite.Properties;
import org.evosuite.rmi.service.ClientNodeImpl;
import org.evosuite.rmi.service.ClientNodeLocal;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should be used only in the Client processes, not the master.
 * Used to initialize and store all the RMI services in the clients
 * 
 * @author arcuri
 *
 */
public class ClientServices {

	private static Logger logger = LoggerFactory.getLogger(ClientServices.class);
	
	private static ClientServices instance = new ClientServices();
	
	private ClientNodeImpl clientNode;
	
	protected ClientServices(){		
	}
	
	public static ClientServices getInstance(){
		return instance;
	}

	public boolean registerServices() {

		try{
			int port = Properties.PROCESS_COMMUNICATION_PORT;
			Registry registry = LocateRegistry.getRegistry(port);
			clientNode = new ClientNodeImpl(registry);
			ClientNodeRemote stub = (ClientNodeRemote) UtilsRMI.exportObject(clientNode);
			registry.rebind(clientNode.getClientRmiIdentifier(), stub);
			return clientNode.init();
		} catch(RemoteException e){
			logger.error("Failed to register client services",e);
			return false;
		}
	}

	public ClientNodeLocal getClientNode() {
		return clientNode;
	}
	
	public void stopServices(){
		if(clientNode!=null){
			int i = 0;
			final int tries = 10;
			boolean done = false;
			try {
				while(!done){
					/*
					 * A call from Master could still be active on this node. so we cannot
					 * forcely stop the client, we need to wait
					 */
					done = UnicastRemoteObject.unexportObject(clientNode, false);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {						
					}
					i++;
					if(i>=tries){
						logger.error("Tried "+tries+" times to stop RMI ClientNode, giving up");
						break;
					}
				}
			} catch (NoSuchObjectException e) {
				logger.warn("Failed to delete ClientNode RMI instance",e);
			}
			clientNode = null;
		}
	}
}
