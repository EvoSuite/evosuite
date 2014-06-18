package org.evosuite.rmi.service;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.RuntimeVariable;

public class DummyClientNodeImpl extends ClientNodeImpl {

	public DummyClientNodeImpl(){
		
	}
	
	public DummyClientNodeImpl(Registry registry) {
		super(registry);
		// TODO Auto-generated constructor stub
	}

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
	public void trackOutputVariable(RuntimeVariable name, Object value) {
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
	public boolean waitUntilFinished(long timeoutInMs) throws RemoteException,
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
		return "dummy";
	}


}
