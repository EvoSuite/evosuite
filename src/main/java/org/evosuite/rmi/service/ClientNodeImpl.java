package org.evosuite.rmi.service;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNodeImpl implements ClientNodeLocal, ClientNodeRemote{

	private static Logger logger = LoggerFactory.getLogger(ClientNodeImpl.class);
	
	private volatile ClientState state;
	private MasterNodeRemote masterNode;
	private String clientRmiIdentifier;
	protected volatile CountDownLatch latch;
	protected Registry registry;
	
	public ClientNodeImpl(Registry registry){
		this.registry = registry;
		state = ClientState.NOT_STARTED;
		/*
		 * TODO: for now it is a constant because we have only one client
		 */
		clientRmiIdentifier = "ClientNode";
		latch = new CountDownLatch(1);
	}
	
	@Override
	public void startNewSearch() throws RemoteException, IllegalStateException {
		if(!state.equals(ClientState.NOT_STARTED)){
			throw new IllegalArgumentException("Search has already been started");
		}
		changeState(ClientState.STARTED);
		//TODO
	}

	@Override
	public void cancelCurrentSearch() throws RemoteException {
		System.exit(1);
	}

	@Override
	public boolean waitUntilDone(long timeoutInMs) throws RemoteException, InterruptedException {
		return latch.await(timeoutInMs, TimeUnit.MILLISECONDS);
	}

	@Override
	public void changeState(ClientState state) {
		logger.info("Client changing state from "+state+" to "+this.state);
		this.state = state;
		
		if(this.state.equals(ClientState.DONE)){
			latch.countDown();
		}
		
		try {
			masterNode.informChangeOfStateInClient(clientRmiIdentifier, state);
		} catch (RemoteException e) {
			logger.error("Cannot inform master of change of state",e);
		}
	}

	@Override
	public boolean init() {
		try {			
			masterNode = (MasterNodeRemote) registry.lookup(MasterNodeRemote.RMI_SERVICE_NAME);
			masterNode.registerClientNode(clientRmiIdentifier);
			masterNode.informChangeOfStateInClient(clientRmiIdentifier, state);
		} catch (Exception e) {
			logger.error("Error when connecting to master via RMI",e);
			return false;
		}		
		return true;
	}

	public String getClientRmiIdentifier() {
		return clientRmiIdentifier;
	}

}
