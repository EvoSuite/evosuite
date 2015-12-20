package org.evosuite.coverage.epa;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.MethodCall;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

public class EPACoverageSuiteFitness extends TestSuiteFitnessFunction {

	private EPA tempEPA;

	public EPACoverageSuiteFitness() {
		tempEPA = new EPA();
	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> executionResults = runTestSuite(suite);
		for (ExecutionResult executionResult : executionResults) {
			for (MethodCall methodCallexecution : executionResult.getTrace().getMethodCalls()) {
				System.out.println("- " + methodCallexecution.className + "||" + methodCallexecution.methodName);
			}
		}
		return 0;
	}
}
