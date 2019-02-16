package org.evosuite.coverage.epa;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * An abstract transition <FROM_STATE, ACTION, TO_STATE>
 * 
 * @author galeotti
 *
 */
public abstract class EPATransition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4868298136596868246L;
	
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
		Objects.requireNonNull(originState, "Origin State cannot be null");
		Objects.requireNonNull(actionName, "action Name cannot be null");
		Objects.requireNonNull(destinationState, "destination State  cannot be null");
		this.originState = originState;
		this.actionName = actionName;
		this.destinationState = destinationState;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	public String getTransitionName() {
		final String originStateName= this.getOriginState().getName();
		final String actionName = this.getActionName();
		final String destStateName= this.getDestinationState().getName();
		String transitionName = String.format("[%s,%s,%s]", originStateName, actionName, destStateName);
		return transitionName;
	}

}
