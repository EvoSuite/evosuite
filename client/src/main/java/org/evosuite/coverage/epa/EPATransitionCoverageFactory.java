package org.evosuite.coverage.epa;

import java.util.Set;

public class EPATransitionCoverageFactory extends EPAFitnessFactory {

	public EPATransitionCoverageFactory(String className, EPA epa) {
		super(className, epa);
	}

	@Override
	protected EPATransitionCoverageTestFitness getCoverageGoalFromTransition(EPATransition t) {
		return new EPATransitionCoverageTestFitness(new EPATransitionCoverageGoal(getClassName(), getEpa(), t));
	}

	@Override
	protected Set<EPATransition> getGoalTransitions() {
		return getEpa().getTransitions();
	}

	
}