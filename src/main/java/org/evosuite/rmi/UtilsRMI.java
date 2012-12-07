package org.evosuite.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.evosuite.utils.Randomness;

public class UtilsRMI {

	/**
	 * This is a wrapper over UnicastRemoteObject.exportObject,
	 * as it does not properly handle anonymous/ephemeral ports.
	 * 
	 * In particular, "exportObject(Remote obj)" returns a RemoteStub, and not a Remote reference.  
	 * 
	 * @param obj
	 * @return
	 * @throws RemoteException
	 */
	public static Remote exportObject(Remote obj) throws RemoteException{
		int port = 2000;
		port += Randomness.nextInt(20000);
		
		final int TRIES = 100;
		for(int i=0; i<TRIES; i++){
			try {
				int candidatePort = port+i;
				return UnicastRemoteObject.exportObject(obj,candidatePort);
			} catch (RemoteException e) {								
			}		
		}
		
		return UnicastRemoteObject.exportObject(obj,port);
	}
	
}
