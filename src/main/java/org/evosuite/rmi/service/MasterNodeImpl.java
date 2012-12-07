package org.evosuite.rmi.service;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterNodeImpl implements MasterNodeRemote, MasterNodeLocal {

	private static Logger logger = LoggerFactory.getLogger(MasterNodeImpl.class);
	
	private Registry registry;
	private Set<ClientNodeRemote>  clients;
	
	/**
	 * It is important to keep track of client states for debugging reasons.
	 * For example, if client crash, could be useful to know in which state it was.
	 * We cannot query the client directly in those cases, because it is crashed...
	 * The "key" is the RMI identifier of the client
	 */
	private Map<String,ClientState> clientStates;
	
	public MasterNodeImpl(Registry registry){
		clients = new CopyOnWriteArraySet<ClientNodeRemote>();
		clientStates = new ConcurrentHashMap<String,ClientState>();
		this.registry = registry;
	}
	
	@Override
	public void registerClientNode(String clientRmiIdentifier)
			throws RemoteException {
		
		/*
		 * The client should first register its node, and then inform MasterNode
		 * by calling this method
		 */
		
		ClientNodeRemote node = null;
		try {
		 node = (ClientNodeRemote) registry.lookup(clientRmiIdentifier);
		} catch (Exception e) {
			logger.error("Error when client "+clientRmiIdentifier+" tries to register to master",e);
			return;
		}
		synchronized(clients){
			clients.add(node);
			clients.notifyAll();
		}
	}

	@Override
	public void informChangeOfStateInClient(String clientRmiIdentifier,
			ClientState state) throws RemoteException {
		clientStates.put(clientRmiIdentifier, state);		
	}

	@Override
	public String getSummaryOfClientStatuses() {
		if(clientStates.isEmpty()){
			return "No client has registered";
		}
		String summary = "";
		for(String id : clientStates.keySet()){
			ClientState state = clientStates.get(id);
			summary += id + ": "+state+"\n";
		}
		return summary;
	}

	@Override
	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs) throws InterruptedException {
		
		long start = System.currentTimeMillis();
		
		/*
		 * TODO: this will be a parameter
		 */
		int numberOfExpectedClients = 1;
		
		synchronized(clients){
			while(clients.size() != numberOfExpectedClients){
				long elapsed = System.currentTimeMillis() - start;
				long timeRemained = timeoutInMs - elapsed;
				if(timeRemained <= 0){
					return null;
				}
				clients.wait(timeRemained);
			}
			return Collections.unmodifiableSet(clients);
		}
	}

}
