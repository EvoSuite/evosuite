package org.evosuite.coverage.epa;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public class EPACoverageSuiteFitness extends TestSuiteFitnessFunction {

	private EPA tempEPA;

	public EPACoverageSuiteFitness() {
		tempEPA = new EPA();
	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		return 0;
	}
}
