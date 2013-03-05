package org.evosuite.rmi.service;

import java.util.Collection;
import java.util.Set;

import org.evosuite.utils.Listenable;

/**
 * Master Node view in the master process.  
 * @author arcuri
 *
 */
public interface MasterNodeLocal extends Listenable<ClientStateInformation>{
	
	public String getSummaryOfClientStatuses();
	
	public Collection<ClientState> getCurrentState();

	public Collection<ClientStateInformation> getCurrentStateInformation();

	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs) throws InterruptedException;
	
	public void cancelAllClients();
}
