package org.evosuite.coverage.aes.branch;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class UnreachableBranchCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 1L;

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		return 1;
	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		return 0;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof UnreachableBranchCoverageTestFitness;
	}

	@Override
	public String getTargetClass() {
		return null;
	}

	@Override
	public String getTargetMethod() {
		return null;
	}

}
