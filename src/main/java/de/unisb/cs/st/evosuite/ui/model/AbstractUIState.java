package de.unisb.cs.st.evosuite.ui.model;

import java.io.Serializable;
import java.util.*;

import de.unisb.cs.st.evosuite.ui.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.ui.GraphVizEnvironment;
import de.unisb.cs.st.evosuite.utils.ListUtil;
import de.unisb.cs.st.evosuite.utils.Randomness;
import de.unisb.cs.st.evosuite.utils.TriBoolean;

public abstract class AbstractUIState implements Serializable {
	private static final long serialVersionUID = 1L;

	protected static UnknownUIState unknownUIState(AbstractUIState uiState) {
		return (uiState instanceof UnknownUIState) ? (UnknownUIState)uiState : new UnknownUIState(uiState);
	}

	public abstract List<WindowDescriptor> getTargetableWindowDescriptors();

	public abstract void addTransition(DescriptorBoundUIAction<?> action, UIState toState);

	/**
	 * Used to obtain the UIState we would be in after executing the given action (without executing it).
	 * If the action can not be executed in this state because its descriptor doesn't match, we return null.
	 * 
	 * If we already executed the action in the past, the observed resulting state is returned.
	 * If we did not execute the action in the past and the action can be executed, an UnknownUIState object is returned.
	 * 
	 * @param action
	 * @return if known the state resulting from this action, otherwise an UnknownUIState if the action matches or null if it doesn't 
	 */
	public abstract AbstractUIState getTransition(DescriptorBoundUIAction<?> action);
	
	public abstract Map<DescriptorBoundUIAction<?>, ? extends AbstractUIState> getTransitions();

	public abstract List<UIActionTargetDescriptor> getActionTargetDescriptors();
	
	public AbstractUIState execute(DescriptorBoundUIAction<?> boundAction, UIStateGraph stateGraph, AbstractUIEnvironment environment) {
		boundAction.execute(environment);
		
		UIState newState = environment.waitGetNewState(stateGraph);
		this.addTransition(boundAction, newState);
		return newState;
	}
	
	public TriBoolean canExecuteActionSequence(List<DescriptorBoundUIAction<?>> actionSequence) {
		if (actionSequence.isEmpty()) {
			return TriBoolean.True;
		}
		
		DescriptorBoundUIAction<?> firstAction = actionSequence.get(0);
		AbstractUIState nextState = this.getTransition(firstAction);
		return nextState == null ? TriBoolean.False : nextState.canExecuteActionSequence(ListUtil.tail(actionSequence));
	}

	public TriBoolean canExecuteAction(DescriptorBoundUIAction<?> action) {
		List<DescriptorBoundUIAction<?>> seq = Arrays.<DescriptorBoundUIAction<?>>asList(action); 
		return this.canExecuteActionSequence(seq);
	}
	
	public boolean isUnknown() {
		return false;
	}

	public boolean isAmbigue() {
		return false;
	}

	public boolean isKnown() {
		return !this.isUnknown();
	}

	public String shortString() {
		return this.toString();
	}

	public String graphVizId(GraphVizEnvironment env) {
		return "cluster_" + env.getId(this);
	}

	public DescriptorBoundUIAction<?> getRandomDescriptorBoundAction() {
		List<UIActionTargetDescriptor> actionTgts = this.getActionTargetDescriptors(); 
		UIActionTargetDescriptor descriptor = Randomness.choice(actionTgts);

		if (descriptor != null) {
			List<DescriptorBoundUIAction<?>> actions = descriptor.getActions();
			return Randomness.choice(actions);
		}
		
		return null;
	}
}