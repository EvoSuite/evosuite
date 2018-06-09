package org.evosuite.coverage.epa;

public class EPANormalTransition extends EPATransition {

	public EPANormalTransition(EPAState originState, String actionName, EPAState destinationState) {
		super(originState, actionName, destinationState);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1577296081336234589L;

	@Override
	public String toString() {
		return "EPANormalTransition{" + this.getOriginState() + "," + this.getActionName() + ","
				+ this.getDestinationState() + "}";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj instanceof EPANormalTransition) {
			EPANormalTransition other = (EPANormalTransition) obj;
			return this.getOriginState().equals(other.getOriginState())
					&& this.getActionName().equals(other.getActionName())
					&& this.getDestinationState().equals(other.getDestinationState());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.getOriginState().hashCode() + this.getActionName().hashCode()
				+ this.getDestinationState().hashCode();
	}

	/**
	 * 
	 */

}
