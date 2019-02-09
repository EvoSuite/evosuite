package org.evosuite.coverage.aes.method;

import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;

public class UnreachableMethodCoverageTestFitness extends MethodCoverageTestFitness {

	private static final long serialVersionUID = -1696168329265661620L;

	public UnreachableMethodCoverageTestFitness() throws IllegalArgumentException {
		super("", "");
	}

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
        return 1.0;
	}
}
