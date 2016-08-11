package org.evosuite.coverage.epa;

import java.util.List;
import java.util.stream.Collectors;

import org.evosuite.testsuite.AbstractFitnessFactory;

public class EPATransitionCoverageFactory extends AbstractFitnessFactory<EPATransitionCoverageTestFitness> {

	private final EPA epa;
	private final String className;

	public EPATransitionCoverageFactory(String className, EPA epa) {
		this.className = className;
		this.epa = epa;
	}

	@Override
	public List<EPATransitionCoverageTestFitness> getCoverageGoals() {
		return computeCoverageGoals();
	}

	private List<EPATransitionCoverageTestFitness> computeCoverageGoals() {
		List<EPATransitionCoverageTestFitness> coverage_goals = epa.getTransitions().stream()
				.map(t -> new EPATransitionCoverageTestFitness(new EPATransitionCoverageGoal(className, epa, t)))
				.collect(Collectors.toList());
		return coverage_goals;
	}
}

// ooooo |