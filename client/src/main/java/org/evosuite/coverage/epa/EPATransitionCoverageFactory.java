package org.evosuite.coverage.epa;

import java.util.Set;

public class EPATransitionCoverageFactory extends EPAFitnessFactory {

	public EPATransitionCoverageFactory(String className, EPA epa) {
		super(className, epa);
	}

	@Override
	protected Set<EPATransition> getGoalTransitions() {
		return getEpa().getTransitions();
	}
}