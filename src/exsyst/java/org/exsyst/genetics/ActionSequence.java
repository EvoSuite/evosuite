package org.exsyst.genetics;

import java.io.Serializable;
import java.util.*;

import org.evosuite.utils.HashUtil;
import org.evosuite.utils.Randomness;

import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.states.AbstractUIState;
import org.exsyst.model.states.UIState;

public class ActionSequence implements Serializable, Cloneable {
	public static class ActionError extends Error {
		private static final long serialVersionUID = 1L;

		public ActionError() {
			super();
		}

		public ActionError(String message, Throwable cause) {
			super(message, cause);
		}

		public ActionError(String message) {
			super(message);
		}

		public ActionError(Throwable cause) {
			super(cause);
		}
	}

	private static final long serialVersionUID = 1L;

	private final LinkedList<AbstractUIState> states = new LinkedList<AbstractUIState>();
	private final LinkedList<DescriptorBoundUIAction<?>> actions = new LinkedList<DescriptorBoundUIAction<?>>();

	public ActionSequence(AbstractUIState initialState) {
		if (initialState == null) {
			throw new IllegalArgumentException("initialState");
		}

		this.states.add(initialState);
	}

	public ActionSequence(ActionSequence fromSequence) {
		this(fromSequence.states.getFirst());
		this.addActionSequenceUnsafe(fromSequence);
		this.repair();
	}

	public ActionSequence(AbstractUIState firstState,
	        List<DescriptorBoundUIAction<?>> fromActions) {
		this(firstState);
		this.addActionSequenceUnsafe(fromActions);
		this.repair();
	}

	public List<DescriptorBoundUIAction<?>> getActions() {
		return this.actions;
	}

	public AbstractUIState getFinalState() {
		return this.states.getLast();
	}

	private int stateIdxAfterActionIdx(int actionIdx) {
		if (actionIdx >= this.actions.size() || actionIdx < -1) {
			throw new IllegalArgumentException("actionIdx should be in range -1.."
			        + (this.actions.size() - 1) + ", but is " + actionIdx);
		}

		return actionIdx + 1;
	}

	AbstractUIState stateAfterActionIdx(int actionIdx) {
		int stateIdx = stateIdxAfterActionIdx(actionIdx);
		AbstractUIState result = this.states.get(stateIdx);

		assert (result != null);
		return result;
	}

	private int stateIdxBeforeActionIdx(int actionIdx) {
		if (actionIdx > this.actions.size() || actionIdx < 0) {
			throw new IllegalArgumentException("actionIdx should be in range 0.."
			        + (this.actions.size()) + ", but is " + actionIdx);
		}

		return actionIdx;
	}

	AbstractUIState stateBeforeActionIdx(int actionIdx) {
		int stateIdx = stateIdxBeforeActionIdx(actionIdx);
		AbstractUIState result = this.states.get(stateIdx);

		assert (result != null);
		return result;
	}

	private int actionIdxAfterState(int stateIdx) {
		if (stateIdx < 0 || stateIdx >= this.states.size()) {
			throw new IllegalArgumentException("stateIdx should be in range 0.."
			        + (this.states.size() - 1) + ", but is " + stateIdx);
		}

		return stateIdx;
	}

	private LinkedHashSet<ActionSequence> crossOverPointsDirected(ActionSequence other) {
		LinkedHashSet<ActionSequence> result = new LinkedHashSet<ActionSequence>();

		int thisLastIdx = this.actions.size() - 1;
		int otherLastIdx = other.actions.size() - 1;

		int thisFromIdx = 0;
		int otherToIdx = otherLastIdx;

		AbstractUIState thisFromState = stateAfterActionIdx(thisFromIdx - 1);

		for (int thisToIdx = thisLastIdx; thisToIdx >= thisFromIdx; thisToIdx--) {
			List<DescriptorBoundUIAction<?>> thisActions = this.actions.subList(thisFromIdx,
			                                                                    thisToIdx + 1);
			AbstractUIState thisToState = stateAfterActionIdx(thisToIdx);

			for (int otherFromIdx = 0; otherToIdx >= otherFromIdx; otherFromIdx++) {
				List<DescriptorBoundUIAction<?>> otherActions = other.actions.subList(otherFromIdx,
				                                                                      otherToIdx + 1);

				boolean canAppend = thisToState.canExecuteActionSequence(otherActions).isPossiblyTrue();

				if (canAppend) {
					ActionSequence newSequence = new ActionSequence(thisFromState,
					        thisActions);
					newSequence.addActionSequence(otherActions);
					result.add(newSequence);
				}
			}
		}

		return result;
	}

	// TODO: Implement zipf-distribution based getRandomCrossOverPoint()?

	public LinkedHashSet<ActionSequence> crossOverPoints(ActionSequence other) {
		LinkedHashSet<ActionSequence> result = this.crossOverPointsDirected(other);
		result.addAll(other.crossOverPointsDirected(this));
		return result;
	}

	public void addAction(DescriptorBoundUIAction<?> action, AbstractUIState nextState) {
		if (nextState == null) {
			throw new IllegalArgumentException("nextState");
		} else {
			actions.add(action);
			states.add(nextState);
		}
	}

	public void addAction(DescriptorBoundUIAction<?> action) {
		AbstractUIState lastState = states.getLast();
		AbstractUIState nextState = lastState.getTransition(action);

		if (nextState == null) {
			throw new ActionError(
			        String.format("Trying to add action %s to state %s where it can't be executed",
			                      action, lastState));
		} else {
			this.addAction(action, nextState);
		}
	}

	private void addActionUnsafe(DescriptorBoundUIAction<?> action) {
		AbstractUIState lastState = states.getLast();
		AbstractUIState nextState = lastState.getTransition(action);

		if (nextState == null) {
			this.addAction(action, UIState.unknownUIState(lastState));
		} else {
			this.addAction(action, nextState);
		}
	}

	public boolean canAddActionSequence(List<DescriptorBoundUIAction<?>> actions) {
		AbstractUIState lastState = states.getLast();
		return lastState.canExecuteActionSequence(actions).isPossiblyTrue();
	}

	public boolean canAddActionSequence(ActionSequence actionSequence) {
		return canAddActionSequence(actionSequence.actions);
	}

	public void addActionSequence(List<DescriptorBoundUIAction<?>> actions) {
		for (DescriptorBoundUIAction<?> action : actions) {
			this.addAction(action);
		}
	}

	private void addActionSequenceUnsafe(List<DescriptorBoundUIAction<?>> actions) {
		for (DescriptorBoundUIAction<?> action : actions) {
			this.addActionUnsafe(action);
		}
	}

	public void addActionSequence(ActionSequence actionSequence) {
		addActionSequence(actionSequence.actions);
	}

	private void addActionSequenceUnsafe(ActionSequence fromSequence) {
		addActionSequenceUnsafe(fromSequence.actions);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("ActionSequence[");

		Iterator<DescriptorBoundUIAction<?>> actionIter = actions.iterator();
		Iterator<AbstractUIState> stateIter = states.iterator();
		boolean isFirst = true;

		while (stateIter.hasNext()) {
			AbstractUIState state = stateIter.next();

			if (!isFirst) {
				DescriptorBoundUIAction<?> action = actionIter.next();

				result.append(" ==(");
				result.append(action);
				result.append(")=> ");
			}

			result.append(state.shortString());
			isFirst = false;
		}

		result.append("]");
		return result.toString();
	}

	public String shortString() {
		StringBuilder result = new StringBuilder("");

		Iterator<DescriptorBoundUIAction<?>> actionIter = actions.iterator();
		Iterator<AbstractUIState> stateIter = states.iterator();
		boolean isFirst = true;

		while (stateIter.hasNext()) {
			AbstractUIState state = stateIter.next();

			if (!isFirst) {
				DescriptorBoundUIAction<?> action = actionIter.next();

				result.append(" => ");
				result.append(action.shortString());
				result.append(" => ");
			}

			result.append(state.shortString());
			isFirst = false;
		}

		return result.toString();

	}

	@Override
	public int hashCode() {
		return HashUtil.hashCode(this.states.getFirst(), this.actions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof ActionSequence)) {
			return false;
		}

		ActionSequence other = (ActionSequence) obj;
		return this.actions.equals(other.actions)
		        && this.states.getFirst().equals(other.states.getFirst());
	}

	@Override
	protected Object clone() {
		return new ActionSequence(this);
	}

	public int size() {
		return this.actions.size();
	}

	public void removeUnsafe(int actionIdx) {
		int stateIdx = stateIdxAfterActionIdx(actionIdx);
		this.actions.remove(actionIdx);
		this.states.remove(stateIdx);
	}

	public boolean changeUnsafe(int actionIdx) {
		return this.actions.get(actionIdx).randomize();
	}

	public void insertUnsafe(int actionIdx, DescriptorBoundUIAction<?> action) {
		AbstractUIState prevState = this.stateBeforeActionIdx(actionIdx);
		AbstractUIState newState = prevState.getTransition(action);

		this.actions.add(actionIdx, action);
		this.states.add(this.stateIdxAfterActionIdx(actionIdx), newState);
	}

	public void repair() {
		for (int actionIdx = 0; actionIdx < this.actions.size(); actionIdx++) {
			this.repairAt(actionIdx);
		}
	}

	/*
	 * Currently repairAt() repairs an impossible action by deleting it.
	 * 
	 * TODO: Teach repairAt() to repair impossible actions by trying to
	 * get to a state where it is possible? 
	 */
	private void repairAt(int actionIdx) {
		DescriptorBoundUIAction<?> action = this.actions.get(actionIdx);
		AbstractUIState beforeState = this.stateBeforeActionIdx(actionIdx);

		if (beforeState.canExecuteAction(action).isCertainlyFalse()) {
			this.removeUnsafe(actionIdx);
		} else {
			int afterStateIdx = this.stateIdxAfterActionIdx(actionIdx);
			AbstractUIState afterState = beforeState.getTransition(action);
			this.states.set(afterStateIdx, afterState);
		}
	}

	public boolean insertRandomActionUnsafe() {
		List<Integer> indices = new ArrayList<Integer>(this.states.size());
		
		for (int i = 0; i < indices.size(); i++) {
			indices.set(i, i);
		}

		Randomness.shuffle(indices);

		for (int i = 0; i < indices.size(); i++) {
			int prevStateIdx = indices.get(i);
			AbstractUIState afterState = this.states.get(prevStateIdx);

			DescriptorBoundUIAction<?> action = afterState.getNewRandomDescriptorBoundAction();

			if (action != null) {
				int actionIdx = this.actionIdxAfterState(prevStateIdx);
				this.insertUnsafe(actionIdx, action);
				return true;
			}
		}

		for (int i = 0; i < indices.size(); i++) {
			int prevStateIdx = indices.get(i);
			AbstractUIState afterState = this.states.get(prevStateIdx);

			DescriptorBoundUIAction<?> action = afterState.getRandomDescriptorBoundAction();

			if (action != null) {
				int actionIdx = this.actionIdxAfterState(prevStateIdx);
				this.insertUnsafe(actionIdx, action);
				return true;
			}
		}

		return false;
	}
}
