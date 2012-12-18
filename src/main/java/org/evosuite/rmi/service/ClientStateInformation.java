package org.evosuite.rmi.service;

import java.io.Serializable;

public class ClientStateInformation implements Serializable {

	private static final long serialVersionUID = 9138932966696572234L;

	private ClientState state;

	/** Progress 0-100 */
	private int progress = 0;
	
	/** Achieved coverage 0-100 */
	private int coverage = 0;
	
	/** Additional information to display to user about state */
	private String description = "";
	
	public ClientStateInformation(ClientState state) {
		this.state = state;
	}
	
	public void setState(ClientState state) {
		this.state = state;
	}
	
	public ClientState getState() {
		return state;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}	

}
