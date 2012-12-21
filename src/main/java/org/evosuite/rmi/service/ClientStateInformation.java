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
