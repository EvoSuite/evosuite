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

import java.io.Serializable;

public class ClientStateInformation implements Serializable {

	private static final long serialVersionUID = 9138932966696572234L;

	private ClientState state;

	/** Progress 0-100 */
	private int progress = 0;
	
	/** Achieved coverage 0-100 */
	private int coverage = 0;
	
	private int iteration = 0;
	
	public ClientStateInformation(ClientState state) {
		this.state = state;
	}
	
	public void setState(ClientState state) {
		this.state = state;
	}
	
	public ClientState getState() {
		return state;
	}
	
	public int getOverallProgress() {
		int delta = state.getEndProgress() - state.getStartProgress();
		return state.getStartProgress() + (progress * delta) / 100;
	}
	
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getCoverage() {
		return coverage;
	}

	public void setCoverage(int coverage) {
		this.coverage = coverage;
	}
	

	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	@Override
	public String toString() {
		return state.toString() +" - " + iteration+", "+progress+", "+coverage;
	}
}
