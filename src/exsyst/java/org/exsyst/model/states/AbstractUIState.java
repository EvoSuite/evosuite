package org.exsyst.model.states;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.evosuite.utils.ListUtil;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.TriBoolean;
import org.uispec4j.UIComponent;

import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.UIActionTargetDescriptor;
import org.exsyst.model.WindowDescriptor;
import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksEnvironment;

public abstract class AbstractUIState implements Serializable {
	private static final long serialVersionUID = 1L;

	public static UnknownUIState unknownUIState(AbstractUIState uiState) {
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
	
	public List<DescriptorBoundUIAction<? extends UIComponent>> allActions() {
		List<DescriptorBoundUIAction<? extends UIComponent>> result =
				new ArrayList<DescriptorBoundUIAction<? extends UIComponent>>();
		
		for (UIActionTargetDescriptor actionTarget : this.getActionTargetDescriptors()) {
			result.addAll(actionTarget.getActions());
		}
		
		return result;
	}
	
	public List<DescriptorBoundUIAction<? extends UIComponent>> allUnexploredActions() {
		List<DescriptorBoundUIAction<? extends UIComponent>> result =
				this.allActions();
		
		Iterator<DescriptorBoundUIAction<? extends UIComponent>> iter =
				result.iterator();
		
		while (iter.hasNext()) {
			if (!isUnexplored(iter.next())) {
				iter.remove();
			}
		}
		
		return result;
	}

	public List<DescriptorBoundUIAction<? extends UIComponent>> allActionsShuffledUnexploredFirst() {
		List<DescriptorBoundUIAction<? extends UIComponent>> result =
				this.allActions();
		
		Randomness.shuffle(result);
		
		List<DescriptorBoundUIAction<? extends UIComponent>> explored =
				new LinkedList<DescriptorBoundUIAction<? extends UIComponent>>();
		
		Iterator<DescriptorBoundUIAction<? extends UIComponent>> iter =
				result.iterator();
		
		while (iter.hasNext()) {
			DescriptorBoundUIAction<? extends UIComponent> action = iter.next();

			if (!isUnexplored(action)) {
				iter.remove();
				explored.add(action);
			}
		}
		
		result.addAll(explored);
		
		return result;
	}
	
	private boolean isUnexplored(
			DescriptorBoundUIAction<? extends UIComponent> action) {
		return action.getTimesExecuted() == 0;
	}

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
	
	public DescriptorBoundUIAction<?> getNewRandomDescriptorBoundAction() {
		List<DescriptorBoundUIAction<? extends UIComponent>> actions =
				this.allUnexploredActions();
		
		return Randomness.choice(actions);
	}

	public abstract void addToYWorksEnvironment(YWorksEnvironment env);

}
