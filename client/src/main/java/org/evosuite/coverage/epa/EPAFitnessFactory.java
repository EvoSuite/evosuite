package org.evosuite.coverage.epa;

import org.evosuite.testsuite.AbstractFitnessFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class EPAFitnessFactory extends AbstractFitnessFactory<EPATransitionCoverageTestFitness> {

	private final EPA epa;

	private final String className;

	public EPAFitnessFactory(String className, EPA epa) {
		this.className = className;
		this.epa = epa;
	}

	public EPA getEpa() {
		return epa;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public List<EPATransitionCoverageTestFitness> getCoverageGoals() {
		return getGoalTransitions()
				.stream()
				.map(this::getCoverageGoalFromTransition)
				.collect(Collectors.toList());
	}

	protected abstract EPATransitionCoverageTestFitness getCoverageGoalFromTransition(EPATransition t);

	protected abstract Set<EPATransition> getGoalTransitions();
}
