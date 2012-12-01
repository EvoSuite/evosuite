package org.evosuite.rmi.service;

import java.util.Set;

public interface MasterNodeLocal {
	
	public String getSummaryOfClientStatuses();
	
	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs) throws InterruptedException;
}
