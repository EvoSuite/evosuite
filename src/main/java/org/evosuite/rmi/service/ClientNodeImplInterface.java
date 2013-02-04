package org.evosuite.rmi.service;

public interface ClientNodeImplInterface extends ClientNodeLocal,
		ClientNodeRemote {

	public String getClientRmiIdentifier();
}
