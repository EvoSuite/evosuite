/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestFitnessFunction extends
        FitnessFunction<RegressionTestChromosome> {

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(RegressionTestChromosome regressionTest) {
		ExecutionResult firstResult = runTest(regressionTest.getTheTest());
		ExecutionResult secondResult = runTest(regressionTest.getTheSameTestForTheOtherClassLoader());

		// Measure something based on the two results!

		return 0;
	}

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @return Result of the execution
	 */
	public ExecutionResult runTest(TestChromosome testChromosome) {
		if (testChromosome.getLastExecutionResult() != null
		        && !testChromosome.isChanged()) {
			return testChromosome.getLastExecutionResult();
		}

		TestCase test = testChromosome.getTestCase();
		ExecutionResult result = new ExecutionResult(test, null);

		try {
			result = TestCaseExecutor.getInstance().execute(test);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			logger.error("TG: Exception caught: ", e);
			throw new RuntimeException(e);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}

}
