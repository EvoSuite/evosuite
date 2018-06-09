package org.evosuite.coverage.epa;

public class EPAExceptionalTransition extends EPATransition {

	private final String exceptionThrown;

	public EPAExceptionalTransition(EPAState originState, String actionName, EPAState destinationState,
			String exceptionThrown) {
		super(originState, actionName, destinationState);
		this.exceptionThrown = exceptionThrown;
	}

	@Override
	public String toString() {
		return "EPAExceptionalTransition{" + this.getOriginState() + "," + this.getActionName() + ","
				+ this.getDestinationState() + "," + this.getExceptionThrown() + "}";
	}

	public String getExceptionThrown() {
		return this.exceptionThrown;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj instanceof EPAExceptionalTransition) {
			EPAExceptionalTransition other = (EPAExceptionalTransition) obj;
			return this.getOriginState().equals(other.getOriginState())
					&& this.getActionName().equals(other.getActionName())
					&& this.getDestinationState().equals(other.getDestinationState())
					&& this.getExceptionThrown().equals(other.getExceptionThrown());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.getOriginState().hashCode() + this.getActionName().hashCode()
				+ this.getDestinationState().hashCode() + this.getExceptionThrown().hashCode();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2401297265051877345L;

}
