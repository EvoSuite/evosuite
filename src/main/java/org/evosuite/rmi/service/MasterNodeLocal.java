package org.evosuite.rmi.service;

import java.util.Set;

/**
 * Master Node view in the master process.  
 * @author arcuri
 *
 */
public interface MasterNodeLocal {
	
	public String getSummaryOfClientStatuses();
	
	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs) throws InterruptedException;
}
