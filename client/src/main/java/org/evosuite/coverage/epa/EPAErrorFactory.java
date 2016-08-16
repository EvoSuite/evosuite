package org.evosuite.coverage.epa;

import java.util.HashSet;
import java.util.Set;

public class EPAErrorFactory extends EPAFitnessFactory {

	public EPAErrorFactory(String className, EPA epa) {
		super(className, epa);
	}

	@Override
	protected Set<EPATransition> getGoalTransitions() {
		final EPA epa = getEpa();
		final Set<EPAState> states = epa.getStates();
		final Set<String> actions = epa.getActions();

		final Set<EPATransition> errorTransitions = new HashSet<>();
		states.forEach(state1 -> actions.forEach(action -> states.forEach(state2 -> {
			errorTransitions.add(new EPATransition(state1, action, state2));
		})));
		errorTransitions.removeAll(getEpa().getTransitions());

		return errorTransitions;
	}
}