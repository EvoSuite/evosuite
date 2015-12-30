package org.evosuite.coverage.epa;

public class EPATransition {
	final private EPAState originState;
	final private String actionName;
	final private EPAState destinationState;

	public EPAState getOriginState() {
		return originState;
	}

	public String getActionName() {
		return actionName;
	}

	public EPAState getDestinationState() {
		return destinationState;
	}

	public EPATransition(EPAState originState, String actionName, EPAState destinationState) {
		this.originState = originState;
		this.actionName = actionName;
		this.destinationState = destinationState;
	}
}
