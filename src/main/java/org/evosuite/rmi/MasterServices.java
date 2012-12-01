package org.evosuite.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.evosuite.rmi.service.MasterNodeLocal;
import org.evosuite.rmi.service.MasterNodeRemote;
import org.evosuite.rmi.service.MasterNodeImpl;
import org.evosuite.utils.Randomness;

public class MasterServices {

	private int registryPort = -1;
	
	/**
	 *  We store it to avoid issues with GC
	 */
	private Registry registry;

	private MasterNodeImpl masterNode; 
	
	public boolean startRegistry(){
		
		/*
		 * Unfortunately, it does not seem possible to start a RMI registry on an
		 * ephemeral port. So, we start with a port, and see if free. If not, try the
		 * next one, etc. Note, it is important to start from a random port to avoid issues
		 * with several masters running on same node, eg when experiments on cluster.
		 */
		
		int port = 2000;
		port += Randomness.nextInt(10000);
		
		final int TRIES = 100;
		for(int i=0; i<TRIES; i++){
			try {
				int candidatePort = port+i;
				registry = LocateRegistry.createRegistry(candidatePort);
				registryPort = candidatePort;
				return true;
			} catch (RemoteException e) {								
			}		
		}
		
		return false;
	}
	
	/**
	 * Return the port on which the registry is running.
	 * 
	 * @return a negative value if no registry is running
	 */
	public int getRegistryPort(){
		return registryPort; 
	}
	
	public void registerServices() throws RemoteException{
		masterNode = new MasterNodeImpl(registry);
		MasterNodeRemote stub = (MasterNodeRemote) UnicastRemoteObject.exportObject(masterNode,registryPort);
		registry.rebind(MasterNodeRemote.RMI_SERVICE_NAME, stub);
	}
	


	public MasterNodeLocal getMasterNode() {
		return masterNode;
	}
}
