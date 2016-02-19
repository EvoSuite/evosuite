/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.rmi.service;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.evosuite.Properties;
import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.ga.Chromosome;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.statistics.SearchStatistics;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.Listener;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterNodeImpl implements MasterNodeRemote, MasterNodeLocal {

	private static final long serialVersionUID = -6329473514791197464L;

	private static Logger logger = LoggerFactory.getLogger(MasterNodeImpl.class);

	private final Registry registry;
	private final Set<ClientNodeRemote> clients;

	protected final Collection<Listener<ClientStateInformation>> listeners = Collections.synchronizedList(new ArrayList<Listener<ClientStateInformation>>());

	/**
	 * It is important to keep track of client states for debugging reasons. For
	 * example, if client crash, could be useful to know in which state it was.
	 * We cannot query the client directly in those cases, because it is
	 * crashed... The "key" is the RMI identifier of the client
	 */
	private final Map<String, ClientState> clientStates;

	private final Map<String, ClientStateInformation> clientStateInformation;

	public MasterNodeImpl(Registry registry) {
		clients = new CopyOnWriteArraySet<ClientNodeRemote>();
		clientStates = new ConcurrentHashMap<String, ClientState>();
		clientStateInformation = new ConcurrentHashMap<String, ClientStateInformation>();
		this.registry = registry;
	}

	@Override
	public void evosuite_registerClientNode(String clientRmiIdentifier) throws RemoteException {

		/*
		 * The client should first register its node, and then inform MasterNode
		 * by calling this method
		 */

		ClientNodeRemote node = null;
		try {
			node = (ClientNodeRemote) registry.lookup(clientRmiIdentifier);
		} catch (Exception e) {
			logger.error("Error when client " + clientRmiIdentifier
			        + " tries to register to master", e);
			return;
		}
		synchronized (clients) {
			clients.add(node);
			clients.notifyAll();
		}
	}

	@Override
	public void evosuite_informChangeOfStateInClient(String clientRmiIdentifier,
	        ClientState state, ClientStateInformation information) throws RemoteException {
		clientStates.put(clientRmiIdentifier, state);
		// To be on the safe side
		information.setState(state);
		clientStateInformation.put(clientRmiIdentifier, information);
		fireEvent(information);
	}

	@Override
	public Collection<ClientState> getCurrentState() {
		return clientStates.values();
	}

	@Override
	public Collection<ClientStateInformation> getCurrentStateInformation() {
		return clientStateInformation.values();
	}

	@Override
	public String getSummaryOfClientStatuses() {
		if (clientStates.isEmpty()) {
			return "No client has registered";
		}
		String summary = "";
		for (String id : clientStates.keySet()) {
			ClientState state = clientStates.get(id);
			summary += id + ": " + state + "\n";
		}
		return summary;
	}

	@Override
	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs)
	        throws InterruptedException {

		long start = System.currentTimeMillis();

		/*
		 * TODO: this will be a parameter
		 */
		int numberOfExpectedClients = 1;

		synchronized (clients) {
			while (clients.size() != numberOfExpectedClients) {
				long elapsed = System.currentTimeMillis() - start;
				long timeRemained = timeoutInMs - elapsed;
				if (timeRemained <= 0) {
					return null;
				}
				clients.wait(timeRemained);
			}
			return Collections.unmodifiableSet(clients);
		}
	}

	@Override
	public void cancelAllClients() {
		for (ClientNodeRemote client : clients) {
			try {
				LoggingUtils.getEvoLogger().info("Trying to kill client " + client);
				client.cancelCurrentSearch();
			} catch (RemoteException e) {
				logger.warn("Error while trying to cancel client: " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void evosuite_collectStatistics(String clientRmiIdentifier, Chromosome individual) {
		SearchStatistics.getInstance().currentIndividual(clientRmiIdentifier, individual);
	}

	@Override
	public void evosuite_collectStatistics(String clientRmiIdentifier, RuntimeVariable variable, Object value)
	        throws RemoteException {
		SearchStatistics.getInstance().setOutputVariable(variable, value);
	}

	@Override
	public void evosuite_collectTestGenerationResult(
			String clientRmiIdentifier, List<TestGenerationResult> results)
			throws RemoteException {
		SearchStatistics.getInstance().addTestGenerationResult(results);
		
	}

	@Override
	public void evosuite_flushStatisticsForClassChange(String clientRmiIdentifier)
			throws RemoteException {
		SearchStatistics.getInstance().writeStatisticsForAnalysis();
	}

	@Override
	public void evosuite_updateProperty(String clientRmiIdentifier, String propertyName, Object value)
			throws RemoteException, IllegalArgumentException, IllegalAccessException, NoSuchParameterException {
		Properties.getInstance().setValue(propertyName, value);
	}

	@Override
	public void addListener(Listener<ClientStateInformation> listener) {
		listeners.add(listener);
	}

	@Override
	public void deleteListener(Listener<ClientStateInformation> listener) {
		listeners.remove(listener);
	}

	/**
	 * <p>
	 * fireEvent
	 * </p>
	 * 
	 * @param event
	 *            a T object.
	 */
	public void fireEvent(ClientStateInformation event) {
		for (Listener<ClientStateInformation> listener : listeners) {
			listener.receiveEvent(event);
		}
	}
}
