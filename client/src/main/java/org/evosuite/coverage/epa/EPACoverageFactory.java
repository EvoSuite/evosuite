package org.evosuite.coverage.epa;

import org.evosuite.testsuite.AbstractFitnessFactory;

import java.util.List;

public class EPACoverageFactory extends AbstractFitnessFactory<EPACoverageTestFitness> {

	@Override
	public List<EPACoverageTestFitness> getCoverageGoals() {
		return null;
	}
}
