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

	@Override
	public String toString() {
		return "EPATransition{" +
				"originState=" + originState +
				", actionName='" + actionName + '\'' +
				", destinationState=" + destinationState +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EPATransition that = (EPATransition) o;

		if (!originState.equals(that.originState)) return false;
		if (!actionName.equals(that.actionName)) return false;
		return destinationState.equals(that.destinationState);

	}

	@Override
	public int hashCode() {
		int result = originState.hashCode();
		result = 31 * result + actionName.hashCode();
		result = 31 * result + destinationState.hashCode();
		return result;
	}
}
