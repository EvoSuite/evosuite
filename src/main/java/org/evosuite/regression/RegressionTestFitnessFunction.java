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

	protected static TestCaseExecutor executor = TestCaseExecutor.getInstance();

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
			result = executor.execute(test);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			logger.error("TG: Exception caught: ", e);
			System.exit(1);
		}

		// System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
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
