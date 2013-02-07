package org.evosuite.rmi.service;

import java.rmi.RemoteException;

import org.evosuite.ga.Chromosome;

public class DummyClientNodeImpl implements ClientNodeImplInterface {

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changeState(ClientState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeState(ClientState state,
			ClientStateInformation information) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateStatistics(Chromosome individual) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trackOutputVariable(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void waitUntilDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startNewSearch() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelCurrentSearch() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean waitUntilDone(long timeoutInMs) throws RemoteException,
			InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doCoverageAnalysis() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClientRmiIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}


}
