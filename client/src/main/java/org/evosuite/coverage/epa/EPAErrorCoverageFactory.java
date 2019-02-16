package org.evosuite.coverage.epa;

import java.util.HashSet;
import java.util.Set;

public class EPAErrorCoverageFactory extends EPAFitnessFactory {

	public EPAErrorCoverageFactory(String className, EPA epa) {
		super(className, epa);
	}

	@Override
	protected EPATransitionCoverageTestFitness getCoverageGoalFromTransition(EPATransition t) {
		return new EPATransitionCoverageTestFitness(new EPATransitionCoverageGoal(getClassName(), getEpa(), t, true));
	}

	/**
	 * Goals are transitions that do not belong to the EPA automata.
	 */
	@Override
	protected Set<EPATransition> getGoalTransitions() {
		final EPA epa = getEpa();
		final Set<EPAState> states = epa.getStates();
		final Set<String> actions = epa.getActions();

		final Set<EPATransition> non_epa_transitions = createCartesianProduct(states, actions);
		Set<EPATransition> epa_transitions = getEpa().getTransitions();
		non_epa_transitions.removeAll(epa_transitions);

		return non_epa_transitions;
	}

	/**
	 * Computes the cartesian product EpaState x EpaAction x EpaState
	 * 
	 * @param states
	 *            the set of EPA states
	 * @param actions
	 *            the set of EPA actions
	 * @return a set of transitions with the cartesian product
	 */
	private static Set<EPATransition> createCartesianProduct(final Set<EPAState> states, final Set<String> actions) {
		final Set<EPATransition> errorTransitions = new HashSet<>();
		for (EPAState stateFrom : states) {
			for (String action : actions) {
				for (EPAState stateTo : states) {
					errorTransitions.add(new EPANormalTransition(stateFrom, action, stateTo));
				}
			}
		}
		return errorTransitions;
	}
}