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
