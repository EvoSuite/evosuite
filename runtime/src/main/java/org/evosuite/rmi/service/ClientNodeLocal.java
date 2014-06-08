package org.evosuite.rmi.service;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.Publisher;

/**
 * Client Node view in the client process.
 * @author arcuri
 *
 */
public interface ClientNodeLocal extends Publisher {

	public boolean init();

	public void changeState(ClientState state);

	public void changeState(ClientState state, ClientStateInformation information);

	public void updateStatistics(Chromosome individual);

	public void waitUntilDone();
}
